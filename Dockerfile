# Weather AI Server - Dockerfile

# Этап сборки
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Копируем файлы сборки
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties ./

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN gradle build --no-daemon

# Этап запуска
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Устанавливаем timezone
RUN apk add --no-cache tzdata
ENV TZ=Europe/Moscow

# Копируем JAR из этапа сборки
COPY --from=build /app/build/libs/McpClient-1.0-SNAPSHOT.jar ./app.jar

# Создаем директории для данных и логов
RUN mkdir -p /app/data /app/logs

# Переменные окружения (будут переопределены при запуске)
ENV OPENROUTER_API_KEY=""
ENV DATABASE_URL="jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE"

# Порт
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]
