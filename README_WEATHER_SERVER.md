# 🌤️ Weather AI Server

AI-агент на Kotlin, который использует OpenRouter для периодического запроса актуальной погоды в Москве через LLM и сохраняет результаты в SQL базу данных.

## ✨ Особенности

- 🤖 **AI Agent** - интеллектуальный агент для запроса погоды
- 🔄 **Автоматические запросы** - каждую минуту обновляет информацию о погоде
- 💾 **SQL Database** - сохраняет все ответы от LLM (H2 или PostgreSQL)
- 📝 **Подробное логирование** - все операции логируются
- 🌐 **REST API** - доступ к данным через HTTP endpoints
- 🚀 **Production-ready** - готов к развертыванию на VPS

## 🏗️ Архитектура

```
┌─────────────────┐
│  Weather Agent  │
│  (Scheduler)    │
└────────┬────────┘
         │ каждую минуту
         ▼
┌─────────────────┐      ┌──────────────┐
│ OpenRouter      │◄─────┤ LLM Request  │
│ Client          │      │ (GPT-4o-mini)│
└────────┬────────┘      └──────────────┘
         │
         │ сохранение
         ▼
┌─────────────────┐
│  SQL Database   │
│  (H2/PostgreSQL)│
└─────────────────┘
```

## 📋 Требования

- Java 17 или выше
- Gradle 8.0+ (или используйте Gradle Wrapper)
- OpenRouter API Key (получите на https://openrouter.ai/)

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd McpClient
```

### 2. Настройка переменных окружения

```bash
export OPENROUTER_API_KEY="ваш_ключ_от_openrouter"
export DATABASE_URL="jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE"
```

Для получения OpenRouter API ключа:
1. Зарегистрируйтесь на https://openrouter.ai/
2. Перейдите в раздел "Keys"
3. Создайте новый API ключ
4. Скопируйте и используйте ключ

### 3. Сборка проекта

```bash
./gradlew clean build
```

### 4. Запуск сервера

```bash
java -jar build/libs/McpClient-1.0-SNAPSHOT.jar
```

Сервер запустится на `http://localhost:8080`

## 🔌 API Endpoints

### Проверка здоровья сервера

```bash
curl http://localhost:8080/health
```

Ответ: `OK`

### Получить последние 10 записей о погоде

```bash
curl http://localhost:8080/weather/latest
```

Ответ:
```json
[
  {
    "id": 1,
    "timestamp": "2026-02-03T10:15:30Z",
    "weatherResponse": "В Москве сейчас облачно, температура -5°C...",
    "modelUsed": "openai/gpt-4o-mini"
  }
]
```

### Получить все записи

```bash
curl http://localhost:8080/weather/all
```

### Ручной запрос погоды

```bash
curl http://localhost:8080/weather/check-now
```

## 📊 База данных

### Структура таблицы `weather_records`

| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | INTEGER | Уникальный идентификатор |
| `timestamp` | TIMESTAMP | Время запроса |
| `weather_response` | TEXT | Ответ от LLM |
| `model_used` | VARCHAR(100) | Модель LLM |

### Просмотр данных

По умолчанию используется H2 Database. Данные сохраняются в `./data/weather_db.*`

Для просмотра см. [DATABASE_GUIDE.md](DATABASE_GUIDE.md)

## 📝 Логирование

Логи сохраняются в:
- **Консоль** - все логи
- **logs/weather-agent.log** - основные логи с ротацией

Формат лога:
```
2026-02-03 10:15:30 [main] INFO  WeatherAgent - === Ответ LLM о погоде ===
2026-02-03 10:15:30 [main] INFO  WeatherAgent - В Москве сейчас облачно...
2026-02-03 10:15:30 [main] INFO  DatabaseFactory - === Сохранение записи в БД ===
2026-02-03 10:15:30 [main] INFO  DatabaseFactory - Запись сохранена с ID: 1
```

### Примеры логов

#### Успешный запрос погоды

```
2026-02-03 10:15:25 [DefaultDispatcher-worker-1] INFO  WeatherAgent - ╔═══════════════════════════════════════════════════╗
2026-02-03 10:15:25 [DefaultDispatcher-worker-1] INFO  WeatherAgent - ║   НАЧАЛО ПРОВЕРКИ ПОГОДЫ В МОСКВЕ                ║
2026-02-03 10:15:25 [DefaultDispatcher-worker-1] INFO  WeatherAgent - ╚═══════════════════════════════════════════════════╝
2026-02-03 10:15:25 [DefaultDispatcher-worker-1] INFO  OpenRouterClient - === Отправка запроса к OpenRouter ===
2026-02-03 10:15:25 [DefaultDispatcher-worker-1] INFO  OpenRouterClient - Модель: openai/gpt-4o-mini
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  OpenRouterClient - === Получен ответ от OpenRouter ===
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  OpenRouterClient - Ответ: В Москве сейчас облачно, температура -5°C...
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  DatabaseFactory - === Сохранение записи в БД ===
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  DatabaseFactory - Запись сохранена с ID: 1
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  WeatherAgent - ╔═══════════════════════════════════════════════════╗
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  WeatherAgent - ║   ПРОВЕРКА ПОГОДЫ ЗАВЕРШЕНА (ID: 1)              ║
2026-02-03 10:15:27 [DefaultDispatcher-worker-1] INFO  WeatherAgent - ╚═══════════════════════════════════════════════════╝
```

## 🚢 Развертывание на VPS

Подробная инструкция по развертыванию на VPS находится в [DEPLOYMENT.md](DEPLOYMENT.md)

Краткая версия:

```bash
# На VPS
git clone <repository-url>
cd McpClient
./gradlew clean build

# Настройка systemd сервиса
sudo nano /etc/systemd/system/weather-ai.service
# (см. DEPLOYMENT.md для содержимого)

sudo systemctl enable weather-ai
sudo systemctl start weather-ai
```

## 🗄️ Работа с базой данных

Полное руководство по работе с БД находится в [DATABASE_GUIDE.md](DATABASE_GUIDE.md)

### Быстрые команды SQL

```sql
-- Получить все записи
SELECT * FROM weather_records ORDER BY timestamp DESC;

-- Получить записи за сегодня
SELECT * FROM weather_records 
WHERE DATE(timestamp) = CURRENT_DATE;

-- Статистика по дням
SELECT DATE(timestamp), COUNT(*) 
FROM weather_records 
GROUP BY DATE(timestamp);
```

## 🔧 Конфигурация

### Переменные окружения

- `OPENROUTER_API_KEY` - API ключ OpenRouter (обязательно)
- `DATABASE_URL` - URL базы данных (по умолчанию H2)

### Настройка модели LLM

В файле `OpenRouterClient.kt` можно изменить модель:

```kotlin
suspend fun sendChatCompletion(
    model: String = "openai/gpt-4o-mini", // здесь меняем модель
    messages: List<ChatMessage>
): ChatCompletionResponse
```

Доступные модели: https://openrouter.ai/models

### Настройка интервала запросов

В файле `WeatherServer.kt`:

```kotlin
while (true) {
    delay(1.minutes) // изменить на 5.minutes, 10.minutes и т.д.
    weatherAgent.checkWeather()
}
```

## 📦 Зависимости

- **Ktor** - веб-сервер и HTTP клиент
- **Exposed** - SQL ORM
- **H2 / PostgreSQL** - база данных
- **Kotlinx Serialization** - JSON сериализация
- **Logback** - логирование
- **Kotlinx Coroutines** - асинхронность

## 🧪 Тестирование

```bash
# Запуск тестов
./gradlew test

# Проверка работоспособности
curl http://localhost:8080/health

# Ручной запрос погоды
curl http://localhost:8080/weather/check-now

# Просмотр последних записей
curl http://localhost:8080/weather/latest | jq '.'
```

## 🐛 Решение проблем

### Ошибка: "OPENROUTER_API_KEY environment variable is not set"

```bash
export OPENROUTER_API_KEY="ваш_ключ"
```

### Ошибка: "Port 8080 is already in use"

```bash
# Найти процесс
lsof -i :8080
# Убить процесс
kill -9 PID
```

### Логи не создаются

```bash
# Создать директорию для логов
mkdir -p logs
chmod 755 logs
```

## 📚 Документация

- [DEPLOYMENT.md](DEPLOYMENT.md) - развертывание на VPS
- [DATABASE_GUIDE.md](DATABASE_GUIDE.md) - работа с базой данных
- [OpenRouter API Docs](https://openrouter.ai/docs)

## 🤝 Вклад

Не стесняйтесь создавать issues и pull requests!

## 📄 Лицензия

MIT License

## 👤 Автор

Создано с использованием:
- OpenRouter API
- OpenAI GPT-4o-mini
- Kotlin + Ktor

---

**Удачи! Пусть ваш сервер всегда знает актуальную погоду! 🌤️**
