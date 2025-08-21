#!/bin/bash

# DTG Service Azure 배포 스크립트

echo "🚗 DTG Service Azure 배포 시작..."

# 환경 변수 설정
RESOURCE_GROUP="kt-giga-fms-rg"
LOCATION="koreacentral"
ACR_NAME="ktgigafmsacr"
DTG_SERVICE_NAME="dtg-service"
DTG_IMAGE_NAME="dtg-service:latest"

# 1. Azure Container Registry 로그인
echo "📦 Azure Container Registry 로그인..."
az acr login --name $ACR_NAME

# 2. Docker 이미지 빌드
echo "🔨 Docker 이미지 빌드..."
docker build -t $DTG_IMAGE_NAME .

# 3. ACR에 이미지 푸시
echo "⬆️ ACR에 이미지 푸시..."
docker tag $DTG_IMAGE_NAME $ACR_NAME.azurecr.io/$DTG_IMAGE_NAME
docker push $ACR_NAME.azurecr.io/$DTG_IMAGE_NAME

# 4. Azure Container Instance 배포
echo "🚀 Azure Container Instance 배포..."
az container create \
  --resource-group $RESOURCE_GROUP \
  --name $DTG_SERVICE_NAME \
  --image $ACR_NAME.azurecr.io/$DTG_IMAGE_NAME \
  --dns-name-label $DTG_SERVICE_NAME \
  --ports 8085 \
  --environment-variables \
    REDIS_HOST=$REDIS_HOST \
    REDIS_PORT=$REDIS_PORT \
    CAR_TRACKING_SERVICE_URL=$CAR_TRACKING_SERVICE_URL

echo "✅ DTG Service Azure 배포 완료!"
echo "🌐 서비스 URL: http://$DTG_SERVICE_NAME.$LOCATION.azurecontainer.io:8085"
echo "📊 Swagger UI: http://$DTG_SERVICE_NAME.$LOCATION.azurecontainer.io:8085/swagger-ui.html"
