# ====== BUILD ======
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle 래퍼 복사 및 권한 설정
COPY gradlew ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew

# 의존성 파일 복사 (레이어 캐싱 최적화)
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# Gradle 빌드 (테스트 건너뛰기)
RUN ./gradlew clean bootJar -x test --no-daemon

# ====== RUNTIME ======
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 빌드 결과 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown -R appuser:appgroup /app

# non-root 사용자로 전환
USER appuser

EXPOSE 8080

# Azure Container Apps 최적화 설정
ENV JAVA_TOOL_OPTIONS="-XX:+UseZGC -Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# 헬스체크를 위한 대기 시간
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
