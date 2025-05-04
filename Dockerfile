# 1) Сборка приложения с помощью Gradle
FROM gradle:7.6-jdk17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем скрипт Gradle Wrapper и файлы сборки, делаем gradlew исполняемым
COPY build.gradle settings.gradle gradlew /app/
RUN chmod +x gradlew

# Кэшируем зависимости
COPY gradle /app/gradle
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код и собираем fat JAR, пропуская тесты
COPY src /app/src
RUN ./gradlew clean bootJar -x test --no-daemon

# 2) Подготовка минимального образа для запуска
FROM eclipse-temurin:17-jre-jammy

# Путь к собранному JAR (из стадии сборки)
ARG JAR_FILE=build/libs/*.jar
COPY --from=build /app/${JAR_FILE} /app/app.jar

# Директория запуска
WORKDIR /app

# Открываем порт 80
EXPOSE 80

# Переменные окружения по-умолчанию (переопределяются при docker run или в Compose)
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

# Команда запуска приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]