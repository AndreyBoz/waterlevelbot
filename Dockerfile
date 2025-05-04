# 1) Сборка приложения
FROM gradle:7.6-jdk17 AS build

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем только то, что нужно для скачивания зависимостей
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
RUN ./gradlew --version

RUN ./gradlew dependencies --no-daemon
COPY src /app/src
RUN ./gradlew clean bootJar -x test --no-daemon
FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE=build/libs/*.jar
COPY --from=build /app/${JAR_FILE} /app/app.jar

WORKDIR /app
EXPOSE 80
ENV SERVER_PORT=80 \
    SERVER_ADDRESS=0.0.0.0

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/water_level_bot \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres \
    TELEGRAM_WEBHOOK_PATH= \
    TELEGRAM_BOT_NAME=WaterGuardBot \
    TELEGRAM_BOT_TOKEN= \
    YANDEX_API_KEY= \
    DOMAIN_URL=

ENTRYPOINT ["java","-jar","/app/app.jar"]
