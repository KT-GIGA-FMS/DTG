# 🚗 DTG Service

KT Giga FMS의 DTG(Data Terminal Gateway) 서비스입니다. 차량의 실시간 위치 추적과 운행 관리를 담당합니다.

## 🎯 주요 기능

### 1. 운행 관리
- **운행 시작**: 차량의 운행을 시작하고 추적을 시작
- **운행 종료**: 운행을 종료하고 추적을 중단
- **운행 상태 조회**: 현재 활성 운행 목록 및 상태 확인

### 2. 실시간 추적
- **1초 간격 데이터 전송**: GPS 위치, 속도, 방향 등 실시간 데이터
- **WebSocket 지원**: 프론트엔드와의 실시간 통신
- **Redis 연동**: car-tracking-service와의 데이터 동기화

### 3. 서비스 연동
- **car-tracking-service**: 실시간 추적 데이터 전송
- **analytics-service**: 운행 완료 데이터 전송
- **프론트엔드**: WebSocket을 통한 실시간 데이터 제공

## 🏗️ 아키텍처

```
DTG Service (8080)
├── REST API (운행 시작/종료)
├── WebSocket (실시간 데이터)
├── Scheduled Task (1초마다 데이터 전송)
└── Service Integration
    ├── car-tracking-service
    ├── analytics-service
    └── Frontend
```

## 🚀 API 엔드포인트

### 운행 관리
- `POST /api/v1/dtg/trips/start` - 운행 시작
- `POST /api/v1/dtg/trips/end` - 운행 종료
- `GET /api/v1/dtg/trips/active` - 활성 운행 목록
- `GET /api/v1/dtg/trips/{vehicleId}` - 특정 차량 운행 상태

### 상태 확인
- `GET /api/v1/dtg/health` - 서비스 상태 확인

### WebSocket
- `ws://localhost:8085/ws` - WebSocket 연결
- `/topic/tracking/{vehicleId}` - 특정 차량 추적 데이터
- `/topic/trips/{vehicleId}/start` - 운행 시작 알림
- `/topic/trips/{vehicleId}/end` - 운행 종료 알림

## 🛠️ 기술 스택

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring WebSocket**
- **Spring Data Redis**
- **Spring Scheduler**
- **Lombok**
- **Swagger/OpenAPI**

## 📋 환경 변수

```bash
# Redis 설정
REDIS_HOST=localhost
REDIS_PORT=6379

# Car Tracking Service 설정
CAR_TRACKING_SERVICE_URL=http://localhost:8082
```

## 🚀 실행 방법

### 1. 로컬 실행
```bash
# 프로젝트 빌드
./gradlew clean build -x test

# 애플리케이션 실행
./gradlew bootRun
```

### 2. Docker 실행
```bash
# Docker 이미지 빌드
docker build -t dtg-service .

# Docker 컨테이너 실행
docker run -p 8085:8085 dtg-service
```

### 3. Azure 배포
```bash
# Azure CLI 로그인
az login

# 환경 변수 설정
export REDIS_HOST=<your-redis-host>
export REDIS_PORT=<your-redis-port>
export CAR_TRACKING_SERVICE_URL=<your-car-tracking-service-url>

# 배포 실행
./deploy-azure.sh
```

## 📊 Swagger UI

서비스 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 🔄 데이터 흐름

### 운행 시작
1. DTG 서비스에 운행 시작 요청
2. car-tracking-service에 운행 시작 알림
3. 1초마다 실시간 추적 데이터 전송 시작
4. 프론트엔드에 WebSocket으로 데이터 전송

### 운행 종료
1. DTG 서비스에 운행 종료 요청
2. car-tracking-service에 운행 종료 알림
3. analytics-service에 운행 완료 데이터 전송
4. 프론트엔드에 운행 종료 알림

## 📝 개발 가이드

### 새로운 추적 데이터 추가
1. `TrackingData` DTO에 필드 추가
2. `DtgTrackingService.generateTrackingData()` 메서드 수정
3. 프론트엔드에서 새로운 데이터 수신 처리

### 새로운 서비스 연동
1. `CarTrackingIntegrationService`에 메서드 추가
2. REST API 호출 또는 메시지 전송 로직 구현
3. 에러 처리 및 로깅 추가

## 🐛 문제 해결

### WebSocket 연결 실패
- 브라우저 콘솔에서 연결 상태 확인
- CORS 설정 확인
- 프록시 설정 확인

### Redis 연결 실패
- Redis 서버 상태 확인
- 환경 변수 설정 확인
- 네트워크 연결 상태 확인

### 서비스 간 통신 실패
- 서비스 URL 설정 확인
- 네트워크 연결 상태 확인
- 로그에서 에러 메시지 확인

## 📞 지원

문제가 발생하거나 질문이 있으시면 개발팀에 문의해주세요.
