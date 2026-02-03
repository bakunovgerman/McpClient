# 🚀 Быстрый старт - Weather AI Server

## Минимальные шаги для запуска

### 1. Установите Java 17+

```bash
# Проверка версии
java -version

# Если Java не установлена:
# macOS:
brew install openjdk@17

# Ubuntu/Debian:
sudo apt install openjdk-17-jdk

# Windows:
# Скачайте с https://adoptium.net/
```

### 2. Получите OpenRouter API ключ

1. Откройте https://openrouter.ai/
2. Зарегистрируйтесь или войдите
3. Перейдите в раздел **Keys** (или https://openrouter.ai/keys)
4. Нажмите **Create Key**
5. Скопируйте созданный ключ

### 3. Настройте проект

```bash
# Клонируйте репозиторий (или распакуйте архив)
cd McpClient

# Создайте .env файл
cp .env.example .env

# Отредактируйте .env и вставьте ваш API ключ
nano .env  # или используйте любой текстовый редактор
```

В файле `.env` замените:
```
OPENROUTER_API_KEY=your_openrouter_api_key_here
```

на:
```
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxxxxxxxxx
```

### 4. Запустите сервер

```bash
# Дайте права на выполнение скриптам
chmod +x build-and-run.sh run-weather-server.sh

# Запустите (автоматически соберет и запустит)
./build-and-run.sh
```

Или вручную:

```bash
# Сборка
./gradlew clean build

# Установка переменной окружения
export OPENROUTER_API_KEY="ваш_ключ"

# Запуск
java -jar build/libs/McpClient-1.0-SNAPSHOT.jar
```

### 5. Проверьте работу

Откройте новый терминал и выполните:

```bash
# Проверка здоровья
curl http://localhost:8080/health

# Получить последние записи о погоде
curl http://localhost:8080/weather/latest

# Ручной запрос погоды
curl http://localhost:8080/weather/check-now
```

## 🎉 Готово!

Сервер запущен и каждую минуту запрашивает погоду в Москве через GPT-4o-mini!

### Что происходит?

1. ⏱️ Каждую минуту сервер автоматически запрашивает погоду
2. 🤖 LLM (GPT-4o-mini) генерирует ответ о текущей погоде в Москве
3. 💾 Ответ сохраняется в базу данных (H2)
4. 📝 Все операции логируются в консоль и файл `logs/weather-agent.log`

### Просмотр логов

```bash
# В реальном времени
tail -f logs/weather-agent.log

# Последние 50 строк
tail -n 50 logs/weather-agent.log
```

### Просмотр базы данных

Смотрите подробную инструкцию в [DATABASE_GUIDE.md](DATABASE_GUIDE.md)

Быстрый способ:

```bash
# Установите H2 Database
wget https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar

# Запустите H2 Console
java -jar h2-2.2.224.jar

# Откройте в браузере: http://localhost:8082
# JDBC URL: jdbc:h2:./data/weather_db
# User: (оставьте пустым)
# Password: (оставьте пустым)
```

### Остановка сервера

Нажмите `Ctrl+C` в терминале с сервером

---

## Что дальше?

- 📖 Прочитайте [README_WEATHER_SERVER.md](README_WEATHER_SERVER.md) для полной документации
- 🚢 Разверните на VPS: [DEPLOYMENT.md](DEPLOYMENT.md)
- 🗄️ Узнайте больше о работе с БД: [DATABASE_GUIDE.md](DATABASE_GUIDE.md)

## Решение проблем

### Ошибка: "OPENROUTER_API_KEY is not set"

```bash
# Убедитесь, что файл .env существует
ls -la .env

# Проверьте содержимое
cat .env

# Убедитесь, что ключ установлен
export OPENROUTER_API_KEY="ваш_ключ"
```

### Ошибка: "Port 8080 already in use"

```bash
# Найдите процесс
lsof -i :8080

# Убейте процесс
kill -9 PID
```

### Ошибка при сборке

```bash
# Очистите кэш Gradle
./gradlew clean --refresh-dependencies

# Проверьте версию Java
java -version  # должна быть 17+
```

---

**Приятного использования! 🌤️**
