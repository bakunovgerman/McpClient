# ❓ FAQ - Часто задаваемые вопросы

## Общие вопросы

### Что делает этот сервер?

Weather AI Server - это Kotlin приложение, которое:
- Каждую минуту автоматически запрашивает погоду в Москве через AI (GPT-4o-mini)
- Сохраняет ответы в SQL базу данных
- Предоставляет REST API для доступа к данным
- Логирует все операции

### Зачем использовать AI для погоды?

AI (LLM) может:
- Генерировать понятные описания погоды на естественном языке
- Агрегировать информацию из разных источников
- Давать контекстуальные советы (например, "Возьмите зонт")
- Отвечать на дополнительные вопросы о погоде

---

## Установка и настройка

### Где получить OpenRouter API ключ?

1. Зарегистрируйтесь на https://openrouter.ai/
2. Перейдите в раздел **Keys**: https://openrouter.ai/keys
3. Нажмите **Create Key**
4. Скопируйте ключ и сохраните в безопасном месте
5. Пополните баланс (минимум $5) на https://openrouter.ai/credits

### Сколько стоит использование?

OpenAI GPT-4o-mini стоит очень дешево:
- ~$0.15 за 1M входных токенов
- ~$0.60 за 1M выходных токенов

При запросе погоды каждую минуту:
- ~50 токенов на запрос (prompt)
- ~100 токенов на ответ
- **Итого: ~$0.10 в месяц** (43200 запросов)

### Какая минимальная конфигурация VPS?

Минимальные требования:
- CPU: 1 core
- RAM: 1 GB
- Disk: 10 GB
- OS: Ubuntu 20.04+ / Debian 11+

Рекомендуемые (для production):
- CPU: 2 cores
- RAM: 2 GB
- Disk: 20 GB

### Можно ли использовать другой город?

Да! Измените запрос в `WeatherAgent.kt`:

```kotlin
ChatMessage(
    role = "user",
    content = "Какая сейчас погода в Санкт-Петербурге?" // измените город
)
```

---

## Работа с API

### Как получить данные за определенный период?

По умолчанию `/weather/all` возвращает все записи. Для фильтрации можно:

1. Добавить endpoint в `WeatherServer.kt`:

```kotlin
get("/weather/today") {
    val today = weatherAgent.getWeatherRecordsForToday()
    call.respond(today)
}
```

2. Фильтровать на клиенте:

```bash
# Получить все записи за сегодня
curl -s http://localhost:8080/weather/all | jq '[.[] | select(.timestamp | startswith("2026-02-03"))]'
```

### Как изменить интервал запросов?

В `WeatherServer.kt` измените:

```kotlin
while (true) {
    delay(1.minutes)  // изменить на 5.minutes, 10.minutes и т.д.
    weatherAgent.checkWeather()
}
```

Доступные значения:
- `30.seconds`
- `1.minutes`
- `5.minutes`
- `10.minutes`
- `1.hours`

### Можно ли запрашивать несколько городов?

Да! Измените `WeatherAgent.kt`:

```kotlin
suspend fun checkWeatherMultipleCities() {
    val cities = listOf("Москва", "Санкт-Петербург", "Казань")
    
    cities.forEach { city ->
        val messages = listOf(
            ChatMessage(role = "system", content = "Ты метеоролог."),
            ChatMessage(role = "user", content = "Погода в $city?")
        )
        
        val response = openRouterClient.sendChatCompletion(messages = messages)
        // сохранить в БД
    }
}
```

---

## База данных

### Какую БД лучше использовать?

- **H2** (по умолчанию):
  - ✅ Не требует установки
  - ✅ Простота использования
  - ✅ Отлично для development и небольших нагрузок
  - ❌ Файловая БД (менее надежна)
  
- **PostgreSQL**:
  - ✅ Production-ready
  - ✅ Высокая надежность
  - ✅ Поддержка репликации и бэкапов
  - ❌ Требует установки и настройки

**Рекомендация**: H2 для dev/testing, PostgreSQL для production.

### Как мигрировать с H2 на PostgreSQL?

См. [DATABASE_GUIDE.md](DATABASE_GUIDE.md#миграция-с-h2-на-postgresql)

Краткая версия:
1. Установите PostgreSQL
2. Создайте БД и пользователя
3. Измените `DATABASE_URL`
4. Перезапустите сервер (таблицы создадутся автоматически)
5. Импортируйте данные из H2 (если нужно)

### Как очистить старые записи?

```sql
-- Удалить записи старше 30 дней
DELETE FROM weather_records 
WHERE timestamp < NOW() - INTERVAL '30 days';
```

Или создайте scheduled job (cron):

```bash
# Скрипт для очистки
cat > cleanup_old_records.sh << 'EOF'
#!/bin/bash
psql -U weatherapp -d weather_db -c "DELETE FROM weather_records WHERE timestamp < NOW() - INTERVAL '30 days';"
EOF

chmod +x cleanup_old_records.sh

# Добавить в cron (каждую неделю)
crontab -e
0 0 * * 0 /home/weatherapp/cleanup_old_records.sh
```

### Как сделать резервную копию?

**H2:**
```bash
# Остановить сервер
sudo systemctl stop weather-ai

# Скопировать файлы
cp -r data/ data_backup_$(date +%Y%m%d)

# Запустить сервер
sudo systemctl start weather-ai
```

**PostgreSQL:**
```bash
# Создать бэкап
pg_dump -U weatherapp weather_db > backup_$(date +%Y%m%d).sql

# Восстановить
psql -U weatherapp weather_db < backup_20260203.sql
```

---

## Развертывание

### Как обновить сервер на VPS?

```bash
# 1. Подключиться к VPS
ssh user@your-vps-ip

# 2. Остановить сервис
sudo systemctl stop weather-ai

# 3. Получить обновления
cd ~/weather-ai-server
git pull

# 4. Пересобрать
./gradlew clean build

# 5. Запустить
sudo systemctl start weather-ai

# 6. Проверить логи
sudo journalctl -u weather-ai -f
```

### Как настроить автозапуск?

```bash
# Включить автозапуск
sudo systemctl enable weather-ai

# Проверить статус
sudo systemctl status weather-ai

# Перезагрузить для проверки
sudo reboot
```

### Как настроить HTTPS?

Используйте Nginx + Let's Encrypt:

```bash
# Установить Certbot
sudo apt install certbot python3-certbot-nginx

# Получить сертификат
sudo certbot --nginx -d your-domain.com

# Автообновление
sudo systemctl enable certbot.timer
```

См. [DEPLOYMENT.md](DEPLOYMENT.md#настройка-nginx) для деталей.

---

## Логирование и мониторинг

### Где находятся логи?

- **Файловые логи**: `logs/weather-agent.log`
- **Systemd логи**: `sudo journalctl -u weather-ai`
- **Docker логи**: `docker-compose logs -f weather-ai`

### Как увеличить детальность логов?

В `src/main/resources/logback.xml` измените уровень:

```xml
<!-- Было -->
<root level="INFO">

<!-- Стало -->
<root level="DEBUG">
```

### Как настроить алерты при ошибках?

Вариант 1: Email уведомления через logback

```xml
<appender name="EMAIL" class="ch.qos.logback.classic.net.SMTPAppender">
    <smtpHost>smtp.gmail.com</smtpHost>
    <smtpPort>587</smtpPort>
    <STARTTLS>true</STARTTLS>
    <username>your-email@gmail.com</username>
    <password>your-password</password>
    <to>alert@example.com</to>
    <from>weather-ai@example.com</from>
    <subject>Weather AI Server Error</subject>
    <layout class="ch.qos.logback.classic.PatternLayout">
        <pattern>%date %level %logger - %msg%n</pattern>
    </layout>
</appender>
```

Вариант 2: Monitoring сервисы (Sentry, Prometheus)

---

## Производительность

### Сколько запросов в секунду выдерживает?

С минимальной конфигурацией (1 CPU, 1 GB RAM):
- ~100-200 RPS для read endpoints (`/weather/latest`)
- ~10-20 RPS для write endpoints (`/weather/check-now`)

### Как ускорить работу?

1. **Увеличить ресурсы VPS**
2. **Использовать PostgreSQL** вместо H2
3. **Добавить кэширование** (Redis, Caffeine)
4. **Настроить connection pool**

### Можно ли горизонтально масштабировать?

Да, но потребуются изменения:
1. Использовать PostgreSQL (не H2)
2. Добавить координацию между инстансами (чтобы не дублировать запросы)
3. Использовать load balancer (Nginx, HAProxy)
4. Вынести scheduler в отдельный сервис

---

## Безопасность

### Как защитить API?

Добавьте аутентификацию в Ktor:

```kotlin
install(Authentication) {
    bearer("auth-bearer") {
        authenticate { credential ->
            if (credential.token == "your-secret-token") {
                UserIdPrincipal("api-user")
            } else null
        }
    }
}

routing {
    authenticate("auth-bearer") {
        get("/weather/all") {
            // protected endpoint
        }
    }
}
```

Использование:

```bash
curl -H "Authorization: Bearer your-secret-token" http://localhost:8080/weather/all
```

### Как защитить OpenRouter API ключ?

1. **Никогда не коммитьте** в git
2. Используйте **переменные окружения**
3. На VPS храните в защищенном файле:
   ```bash
   chmod 600 config/env.conf
   ```
4. Используйте **secrets management** (Vault, AWS Secrets Manager)

### Нужен ли firewall?

Да, настройте UFW:

```bash
# Разрешить SSH
sudo ufw allow ssh

# Разрешить HTTP/HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# НЕ открывать 8080 если используете Nginx
# sudo ufw allow 8080/tcp

# Включить firewall
sudo ufw enable
```

---

## Решение проблем

### Сервер падает с OutOfMemoryError

Увеличьте heap size:

```bash
# В systemd сервисе
ExecStart=/usr/bin/java -Xmx512m -jar app.jar

# В Docker
docker run -e JAVA_OPTS="-Xmx512m" ...
```

### Запросы к OpenRouter таймаутят

Увеличьте timeout в `OpenRouterClient.kt`:

```kotlin
val client = HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30000  // 30 секунд
        connectTimeoutMillis = 10000  // 10 секунд
    }
}
```

### База данных заполняется слишком быстро

Уменьшите частоту запросов или настройте автоочистку старых записей.

### Порт 8080 уже занят

Измените порт в `WeatherServer.kt`:

```kotlin
embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
    // ...
}.start(wait = true)
```

---

## Дополнительные возможности

### Можно ли добавить Telegram бота?

Да! Используйте библиотеку `kotlin-telegram-bot`:

```kotlin
val bot = bot {
    token = "YOUR_BOT_TOKEN"
    
    dispatch {
        command("weather") {
            val latest = weatherAgent.getLatestWeatherRecords(1)
            bot.sendMessage(
                chatId = ChatId.fromId(message.chat.id),
                text = latest.firstOrNull()?.weatherResponse ?: "Нет данных"
            )
        }
    }
}

bot.startPolling()
```

### Можно ли добавить веб-интерфейс?

Да! Добавьте HTML endpoint:

```kotlin
get("/") {
    call.respondHtml {
        head { title { +"Weather AI Server" } }
        body {
            h1 { +"Последняя погода в Москве" }
            // fetch data и отобразить
        }
    }
}
```

Или используйте фронтенд фреймворк (React, Vue) с API.

### Можно ли экспортировать данные в CSV?

Да! Добавьте endpoint:

```kotlin
get("/weather/export") {
    val records = weatherAgent.getAllWeatherRecords()
    val csv = buildString {
        appendLine("ID,Timestamp,Model,Weather")
        records.forEach { record ->
            appendLine("${record.id},${record.timestamp},${record.modelUsed},\"${record.weatherResponse}\"")
        }
    }
    
    call.respondText(csv, contentType = ContentType.parse("text/csv"))
}
```

---

## Поддержка

### Где получить помощь?

1. Проверьте документацию:
   - [README_WEATHER_SERVER.md](README_WEATHER_SERVER.md)
   - [DEPLOYMENT.md](DEPLOYMENT.md)
   - [DATABASE_GUIDE.md](DATABASE_GUIDE.md)

2. Проверьте логи:
   ```bash
   tail -f logs/weather-agent.log
   ```

3. Создайте Issue на GitHub

### Как сообщить об ошибке?

Создайте Issue с информацией:
- Версия проекта
- ОС и версия Java
- Шаги для воспроизведения
- Логи ошибки
- Ожидаемое поведение

---

**Если вашего вопроса нет в FAQ, создайте Issue! 📝**
