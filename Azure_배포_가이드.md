# Azure 배포 가이드

## 개요

이 가이드는 Balance Game Spring Boot 애플리케이션을 Azure App Service에 JAR 파일로 배포하는 방법을 설명합니다.

## 사전 요구사항

### 1. Azure CLI 설치
- Windows: [Azure CLI 설치 가이드](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli-windows)
- 설치 후 `az --version` 명령으로 확인

### 2. Azure 계정 및 구독
- Azure 계정 (무료 계정도 가능)
- 활성 구독

### 3. 개발 환경
- Java 17
- Gradle
- Git

## 배포 방법

### 자동 배포 (권장)

#### Windows PowerShell
```powershell
# 관리자 권한으로 PowerShell 실행
.\azure-deploy.ps1
```

#### Linux/Mac
```bash
# 실행 권한 부여
chmod +x azure-deploy.sh

# 배포 실행
./azure-deploy.sh
```

### 수동 배포

#### 1. Azure 로그인
```bash
az login
```

#### 2. 리소스 그룹 생성
```bash
az group create --name balance-game-rg --location "Korea Central"
```

#### 3. PostgreSQL 서버 생성
```bash
# 비밀번호 생성 (16자리 랜덤)
DB_PASSWORD=$(openssl rand -base64 24 | tr -d "=+/" | cut -c1-16)

az postgres flexible-server create \
    --name balance-game-db-server \
    --resource-group balance-game-rg \
    --location "Korea Central" \
    --admin-user balance_admin \
    --admin-password $DB_PASSWORD \
    --sku-name Standard_B1ms \
    --tier Burstable \
    --version 14 \
    --storage-size 32 \
    --public-access 0.0.0.0
```

#### 4. 데이터베이스 생성
```bash
az postgres flexible-server db create \
    --server-name balance-game-db-server \
    --resource-group balance-game-rg \
    --database-name balance_game
```

#### 5. App Service Plan 생성
```bash
az appservice plan create \
    --name balance-game-plan \
    --resource-group balance-game-rg \
    --location "Korea Central" \
    --sku B1 \
    --is-linux
```

#### 6. Web App 생성
```bash
az webapp create \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --plan balance-game-plan \
    --runtime "JAVA:17-java17"
```

#### 7. 애플리케이션 빌드
```bash
./gradlew clean build -x test
```

#### 8. 환경 변수 설정
```bash
# JWT 시크릿 생성
JWT_SECRET=$(openssl rand -base64 64 | tr -d "\n")

# 데이터베이스 연결 문자열
DB_URL="jdbc:postgresql://balance-game-db-server.postgres.database.azure.com:5432/balance_game?sslmode=require"

az webapp config appsettings set \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --settings \
        AZURE_DATABASE_URL="$DB_URL" \
        AZURE_DATABASE_USERNAME="balance_admin" \
        AZURE_DATABASE_PASSWORD="$DB_PASSWORD" \
        JWT_SECRET="$JWT_SECRET" \
        JWT_ACCESS_EXPIRATION="3600000" \
        JWT_REFRESH_EXPIRATION="604800000" \
        SPRING_PROFILES_ACTIVE="azure" \
        CORS_ALLOWED_ORIGINS="https://balance-game-app.azurewebsites.net" \
        WEBSITES_PORT="8080"
```

#### 9. JAR 배포
```bash
az webapp deploy \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --src-path build/libs/Balance_Game-0.0.1-SNAPSHOT.jar \
    --type jar
```

#### 10. 애플리케이션 재시작
```bash
az webapp restart \
    --name balance-game-app \
    --resource-group balance-game-rg
```

## 배포 후 설정

### 1. Google OAuth 설정 (선택사항)
환경 변수에 Google OAuth 클라이언트 정보 추가:
```bash
az webapp config appsettings set \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --settings \
        GOOGLE_CLIENT_ID="your-google-client-id" \
        GOOGLE_CLIENT_SECRET="your-google-client-secret" \
        GOOGLE_REDIRECT_URI="https://balance-game-app.azurewebsites.net/login/oauth2/code/google"
```

### 2. CORS 설정 업데이트
프론트엔드 도메인이 있는 경우:
```bash
az webapp config appsettings set \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --settings \
        CORS_ALLOWED_ORIGINS="https://your-frontend-domain.com,https://balance-game-app.azurewebsites.net"
```

### 3. 커스텀 도메인 설정 (선택사항)
```bash
# 도메인 추가
az webapp config hostname add \
    --webapp-name balance-game-app \
    --resource-group balance-game-rg \
    --hostname your-domain.com

# SSL 인증서 바인딩 (Let's Encrypt 무료 인증서)
az webapp config ssl bind \
    --certificate-thumbprint <thumbprint> \
    --ssl-type SNI \
    --name balance-game-app \
    --resource-group balance-game-rg
```

## 배포 후 확인

### 1. 애플리케이션 상태 확인
```bash
# 헬스 체크
curl https://balance-game-app.azurewebsites.net/actuator/health

# 로그 확인
az webapp log tail --name balance-game-app --resource-group balance-game-rg
```

### 2. 데이터베이스 연결 확인
PostgreSQL 클라이언트를 사용하여 연결 테스트:
```bash
psql -h balance-game-db-server.postgres.database.azure.com -U balance_admin -d balance_game
```

### 3. API 엔드포인트 테스트
```bash
# 기본 API 테스트
curl https://balance-game-app.azurewebsites.net/api/actuator/health

# 질문 목록 조회 (인증 필요 시 토큰 포함)
curl https://balance-game-app.azurewebsites.net/api/questions
```

## 모니터링 및 로깅

### 1. Application Insights 설정 (권장)
```bash
# Application Insights 리소스 생성
az monitor app-insights component create \
    --app balance-game-insights \
    --location "Korea Central" \
    --resource-group balance-game-rg \
    --application-type web

# 연결 문자열 가져오기
APPINSIGHTS_CONNECTION_STRING=$(az monitor app-insights component show \
    --app balance-game-insights \
    --resource-group balance-game-rg \
    --query connectionString -o tsv)

# Web App에 Application Insights 연결
az webapp config appsettings set \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --settings \
        APPLICATIONINSIGHTS_CONNECTION_STRING="$APPINSIGHTS_CONNECTION_STRING"
```

### 2. 로그 스트리밍
```bash
# 실시간 로그 확인
az webapp log tail --name balance-game-app --resource-group balance-game-rg

# 로그 다운로드
az webapp log download --name balance-game-app --resource-group balance-game-rg
```

## 비용 최적화

### 1. 개발/테스트 환경
- App Service Plan: F1 (무료) 또는 B1 (기본)
- PostgreSQL: Burstable B1ms

### 2. 프로덕션 환경
- App Service Plan: S1 이상
- PostgreSQL: General Purpose GP_Gen5_2 이상
- Application Insights 활성화

## 보안 설정

### 1. 네트워크 보안
```bash
# 특정 IP에서만 접근 허용 (선택사항)
az webapp config access-restriction add \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --rule-name "office-ip" \
    --action Allow \
    --ip-address 203.0.113.0/24 \
    --priority 100
```

### 2. HTTPS 강제 적용
```bash
az webapp update \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --https-only true
```

### 3. 최소 TLS 버전 설정
```bash
az webapp config set \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --min-tls-version 1.2
```

## 트러블슈팅

### 1. 일반적인 문제

#### 애플리케이션이 시작되지 않는 경우
```bash
# 로그 확인
az webapp log tail --name balance-game-app --resource-group balance-game-rg

# 환경 변수 확인
az webapp config appsettings list --name balance-game-app --resource-group balance-game-rg
```

#### 데이터베이스 연결 실패
```bash
# 방화벽 규칙 확인
az postgres flexible-server firewall-rule list \
    --name balance-game-db-server \
    --resource-group balance-game-rg

# Azure 서비스 접근 허용
az postgres flexible-server firewall-rule create \
    --name balance-game-db-server \
    --resource-group balance-game-rg \
    --rule-name allow-azure \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0
```

### 2. 성능 최적화

#### JVM 설정
```bash
az webapp config appsettings set \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --settings \
        JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"
```

#### 연결 풀 설정
애플리케이션의 `application-azure.yml`에서 HikariCP 설정 조정

## 업데이트 및 롤백

### 1. 새 버전 배포
```bash
# 새로운 JAR 빌드
./gradlew clean build -x test

# 배포
az webapp deploy \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --src-path build/libs/Balance_Game-0.0.1-SNAPSHOT.jar \
    --type jar
```

### 2. 롤백
```bash
# 이전 배포 버전 확인
az webapp deployment list --name balance-game-app --resource-group balance-game-rg

# 특정 버전으로 롤백
az webapp deployment source config \
    --name balance-game-app \
    --resource-group balance-game-rg \
    --manual-integration \
    --branch production
```

## 정리 (리소스 삭제)

```bash
# 전체 리소스 그룹 삭제 (주의: 모든 리소스가 삭제됩니다)
az group delete --name balance-game-rg --yes --no-wait
```

## 도움말 및 지원

- [Azure App Service 문서](https://docs.microsoft.com/en-us/azure/app-service/)
- [Azure Database for PostgreSQL 문서](https://docs.microsoft.com/en-us/azure/postgresql/)
- [Spring Boot on Azure 가이드](https://docs.microsoft.com/en-us/java/azure/spring-framework/)

## 연락처

문제가 발생하면 다음 로그를 확인하고 필요시 지원팀에 문의:
- Application logs: `az webapp log tail`
- Database logs: Azure Portal > PostgreSQL > Logs
- Application Insights: Azure Portal > Application Insights > Failures