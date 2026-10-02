# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy riêng phần này trước — tận dụng Docker layer cache,
# chỉ tải lại dependency khi build.gradle thật sự đổi, không phải mỗi lần sửa code
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime — chỉ chứa JRE, không chứa JDK/Gradle, image nhẹ hơn nhiều
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]