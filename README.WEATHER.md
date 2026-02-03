# 🌤️ Weather AI Server

> Интеллектуальный сервер на Kotlin для автоматического мониторинга погоды в Москве с использованием AI

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-2.3.7-orange.svg)](https://ktor.io)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📖 Описание

Weather AI Server - это Kotlin приложение, которое:

- 🤖 Использует **OpenRouter API** для взаимодействия с LLM (GPT-4o-mini)
- ⏱️ **Каждую минуту** автоматически запрашивает актуальную погоду в Москве
- 💾 Сохраняет ответы от AI в **SQL базу данных** (H2 или PostgreSQL)
- 📝 Подробно **логирует** все операции
- 🌐 Предоставляет **REST API** для доступа к данным
- 🚀 Готов к **production** развертыванию на VPS или в Docker

---

## ✨ Ключевые возможности

- **AI-Powered**: Использует современные LLM для генерации информативных описаний погоды
- **Автоматизация**: Работает автономно без вмешательства пользователя
- **Persistence**: Все данные сохраняются в реляционной БД
- **Observability**: Подробное логирование операций и ошибок
- **API-First**: Удобный REST API для интеграции
- **Scalable**: Поддержка PostgreSQL для production нагрузок
- **Containerized**: Docker и Docker Compose поддержка

---

## 🚀 Быстрый старт

### Вариант 1: Локальный запуск

```bash
# 1. Клонируйте репозиторий
git clone <repository-url>
cd McpClient

# 2. Создайте файл .env
cp env.example .env
nano .env  # укажите ваш OPENROUTER_API_KEY

# 3. Запустите сервер
./build-and-run.sh
```

### Вариант 2: Docker

```bash
# 1. Настройте .env
echo "OPENROUTER_API_KEY=ваш_ключ" > .env

# 2. Запустите с Docker Compose
docker-compose up -d

# 3. Проверьте логи
docker-compose logs -f weather-ai
```

**Сервер запустится на:** `http://localhost:8080`

---

## 📊 API Endpoints

| Endpoint | Метод | Описание |
|----------|-------|----------|
| `/health` | GET | Проверка здоровья сервера |
| `/weather/latest` | GET | Последние 10 записей о погоде |
| `/weather/all` | GET | Все записи из БД |
| `/weather/check-now` | GET | Ручной запрос погоды |

### Примеры использования

```bash
# Проверка работоспособности
curl http://localhost:8080/health

# Получить последние записи
curl http://localhost:8080/weather/latest | jq '.'

# Инициировать немедленный запрос погоды
curl http://localhost:8080/weather/check-now
```

---

## 🏗️ Архитектура

```
┌──────────────────────────────────────────────────────┐
│                  Weather AI Server                   │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌────────────────┐    ┌─────────────────┐         │
│  │ Weather Agent  │───▶│ OpenRouter      │         │
│  │ (Scheduler)    │    │ Client          │         │
│  └────────┬───────┘    └─────────────────┘         │
│           │                     │                   │
│           │ каждую минуту       │ API Request      │
│           │                     ▼                   │
│           │            ┌─────────────────┐         │
│           │            │ GPT-4o-mini     │         │
│           │            │ (OpenRouter)    │         │
│           │            └─────────────────┘         │
│           │                     │                   │
│           │                     │ Response         │
│           ▼                     ▼                   │
│  ┌─────────────────────────────────────┐           │
│  │    DatabaseFactory (Exposed ORM)    │           │
│  └─────────────────────────────────────┘           │
│                     │                               │
│                     ▼                               │
│  ┌─────────────────────────────────────┐           │
│  │  SQL Database (H2 / PostgreSQL)     │           │
│  │  Table: weather_records             │           │
│  └─────────────────────────────────────┘           │
│                                                      │
│  ┌─────────────────────────────────────┐           │
│  │       REST API (Ktor Server)        │           │
│  └─────────────────────────────────────┘           │
└──────────────────────────────────────────────────────┘
                     ▲
                     │ HTTP Requests
                     │
             ┌───────┴────────┐
             │   API Clients   │
             └────────────────┘
```

---

## 🗄️ Структура базы данных

### Таблица `weather_records`

| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | INTEGER | Уникальный идентификатор (PRIMARY KEY) |
| `timestamp` | TIMESTAMP | Время запроса погоды |
| `weather_response` | TEXT | Текстовый ответ от LLM |
| `model_used` | VARCHAR(100) | Модель LLM (openai/gpt-4o-mini) |

---

## 📚 Документация

Проект содержит подробную документацию для различных сценариев использования:

| Документ | Описание |
|----------|----------|
| [README_WEATHER_SERVER.md](README_WEATHER_SERVER.md) | Полная документация проекта |
| [QUICKSTART.ru.md](QUICKSTART.ru.md) | Быстрый старт на русском |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Развертывание на VPS |
| [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) | Развертывание с Docker |
| [DATABASE_GUIDE.md](DATABASE_GUIDE.md) | Работа с базой данных |
| [TEST_API.md](TEST_API.md) | Тестирование API |

---

## 🔧 Требования

- **Java 17+** (OpenJDK или Eclipse Temurin)
- **Gradle 8.0+** (или используйте Gradle Wrapper)
- **OpenRouter API Key** - получите на [openrouter.ai](https://openrouter.ai/)

Опционально:
- **Docker** & **Docker Compose** - для контейнеризации
- **PostgreSQL 14+** - для production окружения

---

## ⚙️ Конфигурация

### Переменные окружения

```bash
# Обязательно
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxx

# Опционально (по умолчанию H2)
DATABASE_URL=jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE

# Для PostgreSQL
DATABASE_URL=jdbc:postgresql://localhost:5432/weather_db?user=weatherapp&password=xxx
```

### Настройка интервала запросов

По умолчанию: **1 минута**

Изменить в `WeatherServer.kt`:

```kotlin
while (true) {
    delay(1.minutes)  // изменить на 5.minutes, 10.minutes и т.д.
    weatherAgent.checkWeather()
}
```

### Настройка модели LLM

По умолчанию: **openai/gpt-4o-mini**

Изменить в `OpenRouterClient.kt`:

```kotlin
suspend fun sendChatCompletion(
    model: String = "openai/gpt-4o-mini",  // изменить модель здесь
    messages: List<ChatMessage>
): ChatCompletionResponse
```

Доступные модели: https://openrouter.ai/models

---

## 📝 Логирование

Логи сохраняются в двух местах:

1. **Консоль** - вывод в реальном времени
2. **Файл** - `logs/weather-agent.log` (с ротацией)

### Просмотр логов

```bash
# В реальном времени
tail -f logs/weather-agent.log

# Последние 100 строк
tail -n 100 logs/weather-agent.log

# Поиск ошибок
grep ERROR logs/weather-agent.log
```

### Пример логов

```
2026-02-03 10:15:25 [main] INFO  WeatherAgent - ╔═══════════════════════════════════╗
2026-02-03 10:15:25 [main] INFO  WeatherAgent - ║ НАЧАЛО ПРОВЕРКИ ПОГОДЫ В МОСКВЕ   ║
2026-02-03 10:15:25 [main] INFO  WeatherAgent - ╚═══════════════════════════════════╝
2026-02-03 10:15:27 [main] INFO  OpenRouterClient - === Получен ответ от OpenRouter ===
2026-02-03 10:15:27 [main] INFO  DatabaseFactory - Запись сохранена с ID: 1
```

---

## 🧪 Тестирование

```bash
# Запуск unit тестов
./gradlew test

# Проверка работоспособности
curl http://localhost:8080/health

# Автоматические тесты API
chmod +x test.sh
./test.sh
```

См. [TEST_API.md](TEST_API.md) для подробной информации о тестировании.

---

## 🚢 Развертывание

### На VPS

Подробная инструкция: [DEPLOYMENT.md](DEPLOYMENT.md)

```bash
# На сервере
git clone <repository-url>
cd McpClient
./gradlew clean build

# Настройка systemd
sudo nano /etc/systemd/system/weather-ai.service
sudo systemctl enable weather-ai
sudo systemctl start weather-ai
```

### С Docker

Подробная инструкция: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)

```bash
# Создайте .env
echo "OPENROUTER_API_KEY=ваш_ключ" > .env

# Запустите
docker-compose up -d
```

---

## 🛠️ Технологический стек

- **Язык**: Kotlin 1.9.22
- **Веб-фреймворк**: Ktor 2.3.7
- **HTTP клиент**: Ktor Client
- **ORM**: Exposed
- **База данных**: H2 (dev) / PostgreSQL (prod)
- **Сериализация**: Kotlinx Serialization
- **Логирование**: Logback
- **Сборка**: Gradle 8.5
- **Контейнеризация**: Docker

---

## 📈 Мониторинг

### Проверка здоровья

```bash
# HTTP health check
curl http://localhost:8080/health

# Docker healthcheck
docker inspect weather-ai-server | grep Health -A 10
```

### Статистика

```bash
# Количество записей в БД
curl -s http://localhost:8080/weather/all | jq 'length'

# Последняя запись
curl -s http://localhost:8080/weather/latest | jq '.[0]'

# Использование ресурсов (Docker)
docker stats weather-ai-server
```

---

## 🐛 Решение проблем

### Сервер не запускается

```bash
# Проверьте, установлен ли API ключ
echo $OPENROUTER_API_KEY

# Проверьте логи
tail -f logs/weather-agent.log

# Проверьте порт
lsof -i :8080
```

### Ошибка подключения к OpenRouter

```bash
# Проверьте API ключ на https://openrouter.ai/keys
# Убедитесь, что есть баланс на аккаунте
# Проверьте логи для деталей ошибки
grep "OpenRouter" logs/weather-agent.log
```

### Проблемы с БД

```bash
# Проверьте файлы БД (H2)
ls -lh data/

# Проверьте подключение (PostgreSQL)
psql -U weatherapp -d weather_db -c "SELECT version();"
```

---

## 🤝 Вклад

Не стесняйтесь создавать Issues и Pull Requests!

---

## 📄 Лицензия

MIT License - используйте свободно!

---

## 🙏 Благодарности

- [OpenRouter](https://openrouter.ai/) - унифицированный API для LLM
- [Ktor](https://ktor.io/) - асинхронный фреймворк для Kotlin
- [JetBrains Exposed](https://github.com/JetBrains/Exposed) - Kotlin SQL библиотека

---

## 📞 Контакты

Создано с ❤️ используя Kotlin и AI

- OpenRouter API: https://openrouter.ai/
- Ktor Framework: https://ktor.io/
- Kotlin: https://kotlinlang.org/

---

**Наслаждайтесь актуальной погодой! 🌤️**
