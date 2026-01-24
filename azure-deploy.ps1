# Azure Balance Game 배포 PowerShell 스크립트
# 사용법: .\azure-deploy.ps1

param(
    [string]$ResourceGroup = "balance-game-student-rg",
    [string]$Location = "Korea Central",
    [string]$AppServicePlan = "balance-game-student-plan",
    [string]$WebAppName = "balance-game-student-app",
    [string]$DbServerName = "balance-game-student-db",
    [string]$DbName = "balance_game",
    [string]$DbUsername = "balance_admin"
)

# 에러 발생 시 스크립트 중단
$ErrorActionPreference = "Stop"

Write-Host "🚀 Azure Balance Game 배포 시작 (학생 요금제)..." -ForegroundColor Blue

# 함수 정의
function Write-Step {
    param([string]$Message)
    Write-Host "📋 단계: $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️ $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Test-AzureCLI {
    try {
        # Azure CLI 경로들을 확인
        $azPaths = @(
            "${env:ProgramFiles}\Microsoft SDKs\Azure\CLI2\wbin\az.cmd",
            "${env:ProgramFiles(x86)}\Microsoft SDKs\Azure\CLI2\wbin\az.cmd",
            "${env:LOCALAPPDATA}\Programs\Microsoft Azure CLI\Scripts\az.cmd"
        )
        
        foreach ($path in $azPaths) {
            if (Test-Path $path) {
                Write-Success "Azure CLI 발견: $path"
                return $path
            }
        }
        
        # PATH에서 찾기
        $azCmd = Get-Command az -ErrorAction SilentlyContinue
        if ($azCmd) {
            Write-Success "Azure CLI가 PATH에 있습니다: $($azCmd.Source)"
            return "az"
        }
        
        return $null
    }
    catch {
        return $null
    }
}

try {
    # Azure CLI 설치 확인
    Write-Step "Azure CLI 설치 확인"
    $azCommand = Test-AzureCLI
    
    if (-not $azCommand) {
        Write-Error "Azure CLI를 찾을 수 없습니다."
        Write-Host "다음 방법으로 Azure CLI를 설치하세요:" -ForegroundColor Yellow
        Write-Host "1. PowerShell에서: winget install Microsoft.AzureCLI" -ForegroundColor Yellow
        Write-Host "2. 또는 https://aka.ms/installazurecliwindows 에서 다운로드" -ForegroundColor Yellow
        Write-Host "설치 후 PowerShell을 다시 시작하고 스크립트를 실행하세요." -ForegroundColor Yellow
        exit 1
    }
    
    Write-Success "Azure CLI가 설치되어 있습니다."

    # Azure CLI 로그인 확인
    Write-Step "Azure CLI 로그인 상태 확인"
    try {
        if ($azCommand -eq "az") {
            $account = & az account show --query "id" -o tsv 2>$null
        } else {
            $account = & $azCommand account show --query "id" -o tsv 2>$null
        }
        
        if ($LASTEXITCODE -ne 0) {
            throw "Not logged in"
        }
        Write-Success "Azure에 로그인되어 있습니다."
        Write-Success "현재 구독: $account"
    }
    catch {
        Write-Warning "Azure에 로그인되어 있지 않습니다. 로그인을 진행합니다..."
        if ($azCommand -eq "az") {
            & az login
        } else {
            & $azCommand login
        }
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Azure 로그인에 실패했습니다."
            exit 1
        }
    }

    # 학생 구독 확인
    Write-Step "구독 정보 확인"
    if ($azCommand -eq "az") {
        $subscriptions = & az account list --query "[].{name:name, id:id, state:state}" -o table
    } else {
        $subscriptions = & $azCommand account list --query "[].{name:name, id:id, state:state}" -o table
    }
    
    Write-Host "사용 가능한 구독:" -ForegroundColor Blue
    Write-Host $subscriptions
    
    # 리소스 그룹 생성
    Write-Step "리소스 그룹 생성/확인"
    if ($azCommand -eq "az") {
        $rgExists = & az group show --name $ResourceGroup --query "id" -o tsv 2>$null
    } else {
        $rgExists = & $azCommand group show --name $ResourceGroup --query "id" -o tsv 2>$null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "리소스 그룹 '$ResourceGroup'이 이미 존재합니다."
    }
    else {
        if ($azCommand -eq "az") {
            & az group create --name $ResourceGroup --location $Location
        } else {
            & $azCommand group create --name $ResourceGroup --location $Location
        }
        if ($LASTEXITCODE -ne 0) {
            Write-Error "리소스 그룹 생성에 실패했습니다."
            exit 1
        }
        Write-Success "리소스 그룹 '$ResourceGroup' 생성 완료"
    }

    # PostgreSQL 서버 생성 (학생 요금제에 맞게 최소 사양)
    Write-Step "PostgreSQL 서버 생성/확인 (학생 요금제 최적화)"
    if ($azCommand -eq "az") {
        $dbExists = & az postgres flexible-server show --name $DbServerName --resource-group $ResourceGroup --query "id" -o tsv 2>$null
    } else {
        $dbExists = & $azCommand postgres flexible-server show --name $DbServerName --resource-group $ResourceGroup --query "id" -o tsv 2>$null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "PostgreSQL 서버 '$DbServerName'이 이미 존재합니다."
    }
    else {
        # 관리자 비밀번호 생성
        $DbPassword = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 16 | ForEach-Object {[char]$_})
        $DbPassword | Out-File -FilePath "db-credentials.txt" -Encoding UTF8
        
        Write-Warning "PostgreSQL 서버를 생성합니다 (최소 사양). 이 작업은 몇 분 소요될 수 있습니다..."
        
        if ($azCommand -eq "az") {
            & az postgres flexible-server create `
                --name $DbServerName `
                --resource-group $ResourceGroup `
                --location $Location `
                --admin-user $DbUsername `
                --admin-password $DbPassword `
                --sku-name Standard_B1ms `
                --tier Burstable `
                --version 14 `
                --storage-size 32 `
                --public-access 0.0.0.0
        } else {
            & $azCommand postgres flexible-server create `
                --name $DbServerName `
                --resource-group $ResourceGroup `
                --location $Location `
                --admin-user $DbUsername `
                --admin-password $DbPassword `
                --sku-name Standard_B1ms `
                --tier Burstable `
                --version 14 `
                --storage-size 32 `
                --public-access 0.0.0.0
        }
            
        if ($LASTEXITCODE -ne 0) {
            Write-Error "PostgreSQL 서버 생성에 실패했습니다."
            exit 1
        }
        Write-Success "PostgreSQL 서버 '$DbServerName' 생성 완료"
    }

    # 데이터베이스 생성
    Write-Step "데이터베이스 생성/확인"
    if ($azCommand -eq "az") {
        $dbNameExists = & az postgres flexible-server db show --server-name $DbServerName --resource-group $ResourceGroup --database-name $DbName --query "id" -o tsv 2>$null
    } else {
        $dbNameExists = & $azCommand postgres flexible-server db show --server-name $DbServerName --resource-group $ResourceGroup --database-name $DbName --query "id" -o tsv 2>$null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "데이터베이스 '$DbName'이 이미 존재합니다."
    }
    else {
        if ($azCommand -eq "az") {
            & az postgres flexible-server db create `
                --server-name $DbServerName `
                --resource-group $ResourceGroup `
                --database-name $DbName
        } else {
            & $azCommand postgres flexible-server db create `
                --server-name $DbServerName `
                --resource-group $ResourceGroup `
                --database-name $DbName
        }
            
        if ($LASTEXITCODE -ne 0) {
            Write-Error "데이터베이스 생성에 실패했습니다."
            exit 1
        }
        Write-Success "데이터베이스 '$DbName' 생성 완료"
    }

    # App Service Plan 생성 (FREE 티어 사용 - 학생 요금제 최적화)
    Write-Step "App Service Plan 생성/확인 (FREE 티어)"
    if ($azCommand -eq "az") {
        $planExists = & az appservice plan show --name $AppServicePlan --resource-group $ResourceGroup --query "id" -o tsv 2>$null
    } else {
        $planExists = & $azCommand appservice plan show --name $AppServicePlan --resource-group $ResourceGroup --query "id" -o tsv 2>$null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "App Service Plan '$AppServicePlan'이 이미 존재합니다."
    }
    else {
        Write-Warning "FREE 티어 App Service Plan을 생성합니다..."
        if ($azCommand -eq "az") {
            & az appservice plan create `
                --name $AppServicePlan `
                --resource-group $ResourceGroup `
                --location $Location `
                --sku F1 `
                --is-linux
        } else {
            & $azCommand appservice plan create `
                --name $AppServicePlan `
                --resource-group $ResourceGroup `
                --location $Location `
                --sku F1 `
                --is-linux
        }
            
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "F1 생성 실패. B1으로 재시도합니다..."
            if ($azCommand -eq "az") {
                & az appservice plan create `
                    --name $AppServicePlan `
                    --resource-group $ResourceGroup `
                    --location $Location `
                    --sku B1 `
                    --is-linux
            } else {
                & $azCommand appservice plan create `
                    --name $AppServicePlan `
                    --resource-group $ResourceGroup `
                    --location $Location `
                    --sku B1 `
                    --is-linux
            }
        }
        
        if ($LASTEXITCODE -ne 0) {
            Write-Error "App Service Plan 생성에 실패했습니다."
            exit 1
        }
        Write-Success "App Service Plan '$AppServicePlan' 생성 완료"
    }

    # Web App 생성
    Write-Step "Web App 생성/확인"
    if ($azCommand -eq "az") {
        $webAppExists = & az webapp show --name $WebAppName --resource-group $ResourceGroup --query "id" -o tsv 2>$null
    } else {
        $webAppExists = & $azCommand webapp show --name $WebAppName --resource-group $ResourceGroup --query "id" -o tsv 2>$null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Web App '$WebAppName'이 이미 존재합니다."
    }
    else {
        if ($azCommand -eq "az") {
            & az webapp create `
                --name $WebAppName `
                --resource-group $ResourceGroup `
                --plan $AppServicePlan `
                --runtime "JAVA:17-java17"
        } else {
            & $azCommand webapp create `
                --name $WebAppName `
                --resource-group $ResourceGroup `
                --plan $AppServicePlan `
                --runtime "JAVA:17-java17"
        }
            
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Web App 생성에 실패했습니다."
            exit 1
        }
        Write-Success "Web App '$WebAppName' 생성 완료"
    }

    # 환경 변수 설정
    Write-Step "환경 변수 설정"
    
    # DB 비밀번호 가져오기
    if (Test-Path "db-credentials.txt") {
        $DbPassword = Get-Content "db-credentials.txt" -Raw
        $DbPassword = $DbPassword.Trim()
    }
    else {
        Write-Warning "DB 비밀번호 파일을 찾을 수 없습니다."
        $DbPassword = Read-Host "PostgreSQL 관리자 비밀번호를 입력하세요" -AsSecureString
        $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DbPassword))
    }

    # PostgreSQL 연결 문자열 생성
    $DbConnectionString = "jdbc:postgresql://${DbServerName}.postgres.database.azure.com:5432/${DbName}?sslmode=require"

    # JWT 시크릿 생성
    $JwtSecret = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})

    # 환경 변수 설정
    if ($azCommand -eq "az") {
        & az webapp config appsettings set `
            --name $WebAppName `
            --resource-group $ResourceGroup `
            --settings `
                AZURE_DATABASE_URL="$DbConnectionString" `
                AZURE_DATABASE_USERNAME="$DbUsername" `
                AZURE_DATABASE_PASSWORD="$DbPassword" `
                JWT_SECRET="$JwtSecret" `
                JWT_ACCESS_EXPIRATION="3600000" `
                JWT_REFRESH_EXPIRATION="604800000" `
                SPRING_PROFILES_ACTIVE="azure" `
                CORS_ALLOWED_ORIGINS="https://${WebAppName}.azurewebsites.net" `
                CORS_ALLOWED_METHODS="GET,POST,PUT,DELETE,OPTIONS" `
                CORS_ALLOWED_HEADERS="Content-Type,Authorization,X-Requested-With,Cache-Control" `
                LOG_LEVEL="INFO" `
                WEBSITES_PORT="8080"
    } else {
        & $azCommand webapp config appsettings set `
            --name $WebAppName `
            --resource-group $ResourceGroup `
            --settings `
                AZURE_DATABASE_URL="$DbConnectionString" `
                AZURE_DATABASE_USERNAME="$DbUsername" `
                AZURE_DATABASE_PASSWORD="$DbPassword" `
                JWT_SECRET="$JwtSecret" `
                JWT_ACCESS_EXPIRATION="3600000" `
                JWT_REFRESH_EXPIRATION="604800000" `
                SPRING_PROFILES_ACTIVE="azure" `
                CORS_ALLOWED_ORIGINS="https://${WebAppName}.azurewebsites.net" `
                CORS_ALLOWED_METHODS="GET,POST,PUT,DELETE,OPTIONS" `
                CORS_ALLOWED_HEADERS="Content-Type,Authorization,X-Requested-With,Cache-Control" `
                LOG_LEVEL="INFO" `
                WEBSITES_PORT="8080"
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "환경 변수 설정에 실패했습니다."
        exit 1
    }
    Write-Success "환경 변수 설정 완료"

    # JAR 파일 확인
    Write-Step "JAR 파일 확인"
    $JarFile = "build\libs\Balance_Game-0.0.1-SNAPSHOT.jar"
    if (!(Test-Path $JarFile)) {
        Write-Error "JAR 파일을 찾을 수 없습니다: $JarFile"
        Write-Warning "JAR 파일을 먼저 빌드해주세요: .\gradlew.bat clean build -x test"
        exit 1
    }
    Write-Success "JAR 파일 확인됨: $JarFile"

    # JAR 배포
    Write-Step "JAR 파일 배포"
    Write-Warning "JAR 파일을 Azure App Service에 배포합니다..."

    if ($azCommand -eq "az") {
        & az webapp deploy `
            --name $WebAppName `
            --resource-group $ResourceGroup `
            --src-path $JarFile `
            --type jar
    } else {
        & $azCommand webapp deploy `
            --name $WebAppName `
            --resource-group $ResourceGroup `
            --src-path $JarFile `
            --type jar
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "JAR 배포에 실패했습니다."
        exit 1
    }
    Write-Success "JAR 배포 완료"

    # 애플리케이션 재시작
    Write-Step "애플리케이션 재시작"
    if ($azCommand -eq "az") {
        & az webapp restart `
            --name $WebAppName `
            --resource-group $ResourceGroup
    } else {
        & $azCommand webapp restart `
            --name $WebAppName `
            --resource-group $ResourceGroup
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "애플리케이션 재시작에 실패했습니다."
        exit 1
    }
    Write-Success "애플리케이션 재시작 완료"

    # 배포 정보 출력
    Write-Step "배포 완료 정보"
    $WebAppUrl = "https://${WebAppName}.azurewebsites.net"

    Write-Host ""
    Write-Host "🎉 배포가 성공적으로 완료되었습니다! (학생 요금제 최적화)" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 배포 정보:" -ForegroundColor Blue
    Write-Host "- 애플리케이션 URL: $WebAppUrl"
    Write-Host "- 리소스 그룹: $ResourceGroup"
    Write-Host "- Web App 이름: $WebAppName"
    Write-Host "- PostgreSQL 서버: ${DbServerName}.postgres.database.azure.com"
    Write-Host "- 데이터베이스 이름: $DbName"
    Write-Host ""
    Write-Host "💰 예상 월 비용 (학생 요금제):" -ForegroundColor Green
    Write-Host "- App Service (F1 FREE): $0"
    Write-Host "- PostgreSQL (B1ms): ~$12"
    Write-Host "- 총합: ~$12/월 (또는 FREE 크레딧 사용)"
    Write-Host ""
    Write-Host "🔧 추가 설정이 필요한 항목:" -ForegroundColor Yellow
    Write-Host "- Google OAuth 클라이언트 ID/Secret (환경 변수: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET)"
    Write-Host "- CORS 허용 도메인 수정 (현재: https://${WebAppName}.azurewebsites.net)"
    Write-Host ""
    Write-Host "📝 DB 접속 정보는 'db-credentials.txt' 파일에 저장되었습니다." -ForegroundColor Blue
    Write-Host ""
    Write-Host "🌐 애플리케이션 상태 확인: $WebAppUrl/actuator/health"
    Write-Host ""
    Write-Host "📱 몇 분 후 애플리케이션이 시작됩니다. 로그 확인:" -ForegroundColor Blue
    if ($azCommand -eq "az") {
        Write-Host "az webapp log tail --name $WebAppName --resource-group $ResourceGroup"
    } else {
        Write-Host "$azCommand webapp log tail --name $WebAppName --resource-group $ResourceGroup"
    }

}
catch {
    Write-Error "배포 중 오류가 발생했습니다: $($_.Exception.Message)"
    exit 1
}