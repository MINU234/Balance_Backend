#!/bin/bash

# Azure Balance Game 배포 스크립트
# 사용법: ./azure-deploy.sh

set -e

echo "🚀 Azure Balance Game 배포 시작..."

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정 변수
RESOURCE_GROUP="balance-game-rg"
LOCATION="Korea Central"
APP_SERVICE_PLAN="balance-game-plan"
WEB_APP_NAME="balance-game-app"
DB_SERVER_NAME="balance-game-db-server"
DB_NAME="balance_game"
DB_USERNAME="balance_admin"
JAR_FILE="build/libs/Balance_Game-0.0.1-SNAPSHOT.jar"

# 함수 정의
print_step() {
    echo -e "${BLUE}📋 단계: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Azure CLI 로그인 확인
print_step "Azure CLI 로그인 상태 확인"
if ! az account show &> /dev/null; then
    print_warning "Azure에 로그인되어 있지 않습니다. 로그인을 진행합니다..."
    az login
else
    print_success "Azure에 로그인되어 있습니다."
fi

# 구독 확인
SUBSCRIPTION_ID=$(az account show --query "id" -o tsv)
print_success "현재 구독: $SUBSCRIPTION_ID"

# 리소스 그룹 생성
print_step "리소스 그룹 생성/확인"
if az group show --name $RESOURCE_GROUP &> /dev/null; then
    print_success "리소스 그룹 '$RESOURCE_GROUP'이 이미 존재합니다."
else
    az group create --name $RESOURCE_GROUP --location "$LOCATION"
    print_success "리소스 그룹 '$RESOURCE_GROUP' 생성 완료"
fi

# PostgreSQL 서버 생성
print_step "PostgreSQL 서버 생성/확인"
if az postgres flexible-server show --name $DB_SERVER_NAME --resource-group $RESOURCE_GROUP &> /dev/null; then
    print_success "PostgreSQL 서버 '$DB_SERVER_NAME'이 이미 존재합니다."
else
    # 관리자 비밀번호 생성
    DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    echo "생성된 DB 비밀번호: $DB_PASSWORD" > db-credentials.txt
    
    print_warning "PostgreSQL 서버를 생성합니다. 이 작업은 몇 분 소요될 수 있습니다..."
    az postgres flexible-server create \
        --name $DB_SERVER_NAME \
        --resource-group $RESOURCE_GROUP \
        --location "$LOCATION" \
        --admin-user $DB_USERNAME \
        --admin-password $DB_PASSWORD \
        --sku-name Standard_B1ms \
        --tier Burstable \
        --version 14 \
        --storage-size 32 \
        --public-access 0.0.0.0
        
    print_success "PostgreSQL 서버 '$DB_SERVER_NAME' 생성 완료"
fi

# 데이터베이스 생성
print_step "데이터베이스 생성/확인"
if az postgres flexible-server db show --server-name $DB_SERVER_NAME --resource-group $RESOURCE_GROUP --database-name $DB_NAME &> /dev/null; then
    print_success "데이터베이스 '$DB_NAME'이 이미 존재합니다."
else
    az postgres flexible-server db create \
        --server-name $DB_SERVER_NAME \
        --resource-group $RESOURCE_GROUP \
        --database-name $DB_NAME
    print_success "데이터베이스 '$DB_NAME' 생성 완료"
fi

# App Service Plan 생성
print_step "App Service Plan 생성/확인"
if az appservice plan show --name $APP_SERVICE_PLAN --resource-group $RESOURCE_GROUP &> /dev/null; then
    print_success "App Service Plan '$APP_SERVICE_PLAN'이 이미 존재합니다."
else
    az appservice plan create \
        --name $APP_SERVICE_PLAN \
        --resource-group $RESOURCE_GROUP \
        --location "$LOCATION" \
        --sku B1 \
        --is-linux
    print_success "App Service Plan '$APP_SERVICE_PLAN' 생성 완료"
fi

# Web App 생성
print_step "Web App 생성/확인"
if az webapp show --name $WEB_APP_NAME --resource-group $RESOURCE_GROUP &> /dev/null; then
    print_success "Web App '$WEB_APP_NAME'이 이미 존재합니다."
else
    az webapp create \
        --name $WEB_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --plan $APP_SERVICE_PLAN \
        --runtime "JAVA:17-java17"
    print_success "Web App '$WEB_APP_NAME' 생성 완료"
fi

# 환경 변수 설정
print_step "환경 변수 설정"

# DB 비밀번호 가져오기 (기존 파일에서 또는 새로 생성)
if [ -f "db-credentials.txt" ]; then
    DB_PASSWORD=$(grep "생성된 DB 비밀번호:" db-credentials.txt | cut -d' ' -f4)
else
    print_warning "DB 비밀번호 파일을 찾을 수 없습니다. 수동으로 입력해주세요."
    read -s -p "PostgreSQL 관리자 비밀번호를 입력하세요: " DB_PASSWORD
    echo
fi

# PostgreSQL 연결 문자열 생성
DB_CONNECTION_STRING="jdbc:postgresql://${DB_SERVER_NAME}.postgres.database.azure.com:5432/${DB_NAME}?sslmode=require"

# JWT 시크릿 생성
JWT_SECRET=$(openssl rand -base64 64 | tr -d "\n")

az webapp config appsettings set \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --settings \
        AZURE_DATABASE_URL="$DB_CONNECTION_STRING" \
        AZURE_DATABASE_USERNAME="$DB_USERNAME" \
        AZURE_DATABASE_PASSWORD="$DB_PASSWORD" \
        JWT_SECRET="$JWT_SECRET" \
        JWT_ACCESS_EXPIRATION="3600000" \
        JWT_REFRESH_EXPIRATION="604800000" \
        SPRING_PROFILES_ACTIVE="azure" \
        CORS_ALLOWED_ORIGINS="https://${WEB_APP_NAME}.azurewebsites.net" \
        CORS_ALLOWED_METHODS="GET,POST,PUT,DELETE,OPTIONS" \
        CORS_ALLOWED_HEADERS="Content-Type,Authorization,X-Requested-With,Cache-Control" \
        LOG_LEVEL="INFO" \
        WEBSITES_PORT="8080"

print_success "환경 변수 설정 완료"

# JAR 빌드
print_step "Spring Boot 애플리케이션 빌드"
if [ ! -f "gradlew" ]; then
    print_error "gradlew 파일을 찾을 수 없습니다. 프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

print_warning "애플리케이션을 빌드합니다..."
./gradlew clean build -x test

if [ ! -f "$JAR_FILE" ]; then
    print_error "JAR 파일을 찾을 수 없습니다: $JAR_FILE"
    exit 1
fi

print_success "빌드 완료: $JAR_FILE"

# JAR 배포
print_step "JAR 파일 배포"
print_warning "JAR 파일을 Azure App Service에 배포합니다..."

az webapp deploy \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --src-path "$JAR_FILE" \
    --type jar

print_success "JAR 배포 완료"

# 애플리케이션 재시작
print_step "애플리케이션 재시작"
az webapp restart \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP

print_success "애플리케이션 재시작 완료"

# 배포 정보 출력
print_step "배포 완료 정보"
WEB_APP_URL="https://${WEB_APP_NAME}.azurewebsites.net"

echo ""
echo "🎉 배포가 성공적으로 완료되었습니다!"
echo ""
echo "📋 배포 정보:"
echo "- 애플리케이션 URL: $WEB_APP_URL"
echo "- 리소스 그룹: $RESOURCE_GROUP"
echo "- Web App 이름: $WEB_APP_NAME"
echo "- PostgreSQL 서버: ${DB_SERVER_NAME}.postgres.database.azure.com"
echo "- 데이터베이스 이름: $DB_NAME"
echo ""
echo "🔧 추가 설정이 필요한 항목:"
echo "- Google OAuth 클라이언트 ID/Secret (환경 변수: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET)"
echo "- CORS 허용 도메인 수정 (현재: https://${WEB_APP_NAME}.azurewebsites.net)"
echo ""
echo "📝 DB 접속 정보는 'db-credentials.txt' 파일에 저장되었습니다."
echo ""
echo "🌐 애플리케이션 상태 확인: $WEB_APP_URL/actuator/health"
echo ""
echo "📱 몇 분 후 애플리케이션이 시작됩니다. 로그 확인:"
echo "az webapp log tail --name $WEB_APP_NAME --resource-group $RESOURCE_GROUP"