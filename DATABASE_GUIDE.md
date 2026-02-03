# 📊 Руководство по работе с базой данных Weather AI Server

## Содержание
- [Структура базы данных](#структура-базы-данных)
- [Просмотр содержимого БД (H2)](#просмотр-содержимого-бд-h2)
- [Просмотр содержимого БД (PostgreSQL)](#просмотр-содержимого-бд-postgresql)
- [SQL запросы](#sql-запросы)
- [REST API для доступа к данным](#rest-api-для-доступа-к-данным)
- [Резервное копирование](#резервное-копирование)
- [Миграция с H2 на PostgreSQL](#миграция-с-h2-на-postgresql)

---

## Структура базы данных

### Таблица `weather_records`

| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | INTEGER | Уникальный идентификатор (автоинкремент) |
| `timestamp` | TIMESTAMP | Время запроса погоды |
| `weather_response` | TEXT | Текстовый ответ от LLM о погоде |
| `model_used` | VARCHAR(100) | Название использованной модели LLM |

### Пример записи

```sql
id: 1
timestamp: 2026-02-03 10:15:30
weather_response: "В Москве сейчас облачно, температура -5°C. Без осадков, ветер северо-западный 5 м/с."
model_used: "openai/gpt-4o-mini"
```

---

## Просмотр содержимого БД (H2)

### Метод 1: H2 Console (Web интерфейс)

H2 Database имеет встроенную веб-консоль для просмотра данных.

#### Запуск H2 Console

```bash
# Скачиваем H2 JAR (если еще не скачан)
wget https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar

# Запускаем H2 Console
java -jar h2-2.2.224.jar
```

Или добавьте в ваш проект:

```kotlin
// В WeatherServer.kt добавьте перед запуском сервера:
import org.h2.tools.Server

fun main() {
    // Запуск H2 Console на порту 8082
    val h2Server = Server.createWebServer("-web", "-webPort", "8082", "-webAllowOthers").start()
    println("H2 Console доступна по адресу: http://localhost:8082")
    
    // ... остальной код
}
```

#### Подключение через браузер

1. Откройте браузер: `http://localhost:8082` (или `http://ваш_vps_ip:8082`)
2. Заполните параметры подключения:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:./data/weather_db`
   - **User Name**: `` (пусто)
   - **Password**: `` (пусто)
3. Нажмите **Connect**

#### Выполнение запросов

```sql
-- Получить все записи
SELECT * FROM weather_records ORDER BY timestamp DESC;

-- Получить последние 10 записей
SELECT * FROM weather_records ORDER BY timestamp DESC LIMIT 10;

-- Подсчитать количество записей
SELECT COUNT(*) FROM weather_records;
```

### Метод 2: SQL клиенты

#### DBeaver (рекомендуется)

1. Скачайте DBeaver: https://dbeaver.io/download/
2. Создайте новое подключение: **Database → New Database Connection**
3. Выберите **H2**
4. Настройки:
   - **Path**: `/home/weatherapp/weather-ai-server/data/weather_db`
   - **Database/Schema**: `PUBLIC`
5. Нажмите **Test Connection**, затем **Finish**

#### DataGrip (JetBrains)

1. Откройте DataGrip
2. **File → New → Data Source → H2**
3. Настройки:
   - **URL**: `jdbc:h2:/home/weatherapp/weather-ai-server/data/weather_db`
   - **User**: (пусто)
   - **Password**: (пусто)
4. Нажмите **Test Connection**, затем **OK**

### Метод 3: Командная строка (H2 Shell)

```bash
# Запуск H2 Shell
java -cp h2-2.2.224.jar org.h2.tools.Shell

# Параметры подключения:
URL: jdbc:h2:./data/weather_db
Driver: org.h2.Driver
User: (нажмите Enter)
Password: (нажмите Enter)

# Теперь можете выполнять SQL запросы:
sql> SELECT * FROM weather_records ORDER BY timestamp DESC LIMIT 5;
sql> exit
```

---

## Просмотр содержимого БД (PostgreSQL)

### Метод 1: psql (командная строка)

```bash
# Подключение к БД
psql -U weatherapp -d weather_db

# Список таблиц
\dt

# Просмотр структуры таблицы
\d weather_records

# Выполнение запросов
SELECT * FROM weather_records ORDER BY timestamp DESC LIMIT 10;

# Выход
\q
```

### Метод 2: pgAdmin

1. Скачайте pgAdmin: https://www.pgadmin.org/download/
2. Добавьте новый сервер:
   - **Name**: Weather DB
   - **Host**: localhost (или IP вашего VPS)
   - **Port**: 5432
   - **Database**: weather_db
   - **Username**: weatherapp
   - **Password**: ваш_пароль
3. Подключитесь и используйте Query Tool для выполнения запросов

### Метод 3: DBeaver / DataGrip

Аналогично H2, но выберите PostgreSQL в качестве типа подключения.

---

## SQL запросы

### Основные запросы

```sql
-- 1. Получить все записи
SELECT * FROM weather_records;

-- 2. Получить последние 10 записей
SELECT * FROM weather_records 
ORDER BY timestamp DESC 
LIMIT 10;

-- 3. Получить записи за сегодня
SELECT * FROM weather_records 
WHERE DATE(timestamp) = CURRENT_DATE;

-- 4. Получить записи за последний час
SELECT * FROM weather_records 
WHERE timestamp >= NOW() - INTERVAL '1 hour';

-- 5. Подсчитать общее количество записей
SELECT COUNT(*) as total_records FROM weather_records;

-- 6. Получить записи с определенной моделью
SELECT * FROM weather_records 
WHERE model_used = 'openai/gpt-4o-mini';

-- 7. Группировка по дням
SELECT DATE(timestamp) as date, COUNT(*) as count 
FROM weather_records 
GROUP BY DATE(timestamp) 
ORDER BY date DESC;

-- 8. Поиск по содержимому ответа
SELECT * FROM weather_records 
WHERE weather_response LIKE '%дождь%';

-- 9. Получить самую раннюю и самую позднюю запись
SELECT 
    MIN(timestamp) as first_record,
    MAX(timestamp) as last_record
FROM weather_records;

-- 10. Удалить записи старше 30 дней
DELETE FROM weather_records 
WHERE timestamp < NOW() - INTERVAL '30 days';
```

### Аналитические запросы

```sql
-- Статистика по дням
SELECT 
    DATE(timestamp) as date,
    COUNT(*) as total_checks,
    MIN(timestamp) as first_check,
    MAX(timestamp) as last_check
FROM weather_records 
GROUP BY DATE(timestamp) 
ORDER BY date DESC;

-- Статистика по моделям
SELECT 
    model_used,
    COUNT(*) as usage_count,
    MIN(timestamp) as first_use,
    MAX(timestamp) as last_use
FROM weather_records 
GROUP BY model_used;

-- Средняя длина ответов по дням
SELECT 
    DATE(timestamp) as date,
    AVG(LENGTH(weather_response)) as avg_response_length,
    MIN(LENGTH(weather_response)) as min_length,
    MAX(LENGTH(weather_response)) as max_length
FROM weather_records 
GROUP BY DATE(timestamp) 
ORDER BY date DESC;
```

---

## REST API для доступа к данным

Сервер предоставляет REST API endpoints для доступа к данным:

### 1. Получить последние 10 записей

```bash
curl http://localhost:8080/weather/latest
```

Или с указанием количества через параметр (требует модификации кода):

```bash
curl http://localhost:8080/weather/latest?limit=20
```

### 2. Получить все записи

```bash
curl http://localhost:8080/weather/all
```

### 3. Ручной запрос погоды

```bash
curl http://localhost:8080/weather/check-now
```

### 4. Проверка здоровья сервера

```bash
curl http://localhost:8080/health
```

### Примеры с jq (для красивого форматирования JSON)

```bash
# Установка jq
sudo apt install jq -y

# Получить последние записи с форматированием
curl -s http://localhost:8080/weather/latest | jq '.'

# Получить только текст последнего ответа о погоде
curl -s http://localhost:8080/weather/latest | jq '.[0].weatherResponse'

# Получить только временные метки
curl -s http://localhost:8080/weather/latest | jq '.[].timestamp'
```

---

## Резервное копирование

### H2 Database

#### Метод 1: Копирование файлов

```bash
# Остановить сервер
sudo systemctl stop weather-ai

# Создать резервную копию
tar -czf weather_db_backup_$(date +%Y%m%d_%H%M%S).tar.gz data/

# Запустить сервер
sudo systemctl start weather-ai
```

#### Метод 2: SQL Script

```sql
-- Экспорт в SQL скрипт
SCRIPT TO 'backup_weather.sql';

-- Восстановление
RUNSCRIPT FROM 'backup_weather.sql';
```

### PostgreSQL

#### Создание бэкапа

```bash
# Полный бэкап
pg_dump -U weatherapp weather_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Только данные (без структуры)
pg_dump -U weatherapp --data-only weather_db > data_backup.sql

# Только структура (без данных)
pg_dump -U weatherapp --schema-only weather_db > schema_backup.sql

# Бэкап в custom формате (сжатый)
pg_dump -U weatherapp -Fc weather_db > backup.dump
```

#### Восстановление

```bash
# Из SQL файла
psql -U weatherapp weather_db < backup_20260203_101530.sql

# Из custom формата
pg_restore -U weatherapp -d weather_db backup.dump
```

#### Автоматическое резервное копирование (cron)

```bash
# Создаем скрипт бэкапа
nano ~/backup_weather_db.sh
```

Содержимое:

```bash
#!/bin/bash
BACKUP_DIR=~/backups
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR
pg_dump -U weatherapp weather_db > $BACKUP_DIR/weather_backup_$TIMESTAMP.sql
# Удаляем бэкапы старше 30 дней
find $BACKUP_DIR -name "weather_backup_*.sql" -mtime +30 -delete
```

```bash
# Делаем скрипт исполняемым
chmod +x ~/backup_weather_db.sh

# Добавляем в cron (каждый день в 3:00 ночи)
crontab -e

# Добавляем строку:
0 3 * * * /home/weatherapp/backup_weather_db.sh
```

---

## Миграция с H2 на PostgreSQL

### Шаг 1: Экспорт данных из H2

```bash
# Запустите H2 Shell
java -cp h2-2.2.224.jar org.h2.tools.Shell

# Подключитесь к H2 БД
URL: jdbc:h2:./data/weather_db

# Экспортируйте данные
sql> SCRIPT TO 'h2_export.sql';
```

### Шаг 2: Настройка PostgreSQL

```bash
# Создайте БД и пользователя (см. DEPLOYMENT.md)
sudo -u postgres psql
CREATE DATABASE weather_db;
CREATE USER weatherapp WITH ENCRYPTED PASSWORD 'пароль';
GRANT ALL PRIVILEGES ON DATABASE weather_db TO weatherapp;
```

### Шаг 3: Импорт данных

```bash
# Преобразуйте SQL скрипт для PostgreSQL (если нужно)
# Обычно структура совместима благодаря Exposed ORM

# Измените DATABASE_URL в env.conf
DATABASE_URL=jdbc:postgresql://localhost:5432/weather_db?user=weatherapp&password=пароль

# Перезапустите сервер (таблицы создадутся автоматически)
sudo systemctl restart weather-ai

# Импортируйте данные вручную через psql или через API
```

---

## Полезные инструменты

### Веб-интерфейсы для баз данных

1. **Adminer** - легкий веб-интерфейс для SQL
   ```bash
   wget https://www.adminer.org/latest.php -O adminer.php
   php -S localhost:8081 adminer.php
   ```

2. **phpMyAdmin** - для PostgreSQL используйте phpPgAdmin

### Мониторинг БД

```bash
# Размер БД (PostgreSQL)
psql -U weatherapp -d weather_db -c "SELECT pg_size_pretty(pg_database_size('weather_db'));"

# Количество записей
psql -U weatherapp -d weather_db -c "SELECT COUNT(*) FROM weather_records;"

# Дисковое пространство (H2)
du -sh data/
```

---

## Часто задаваемые вопросы

### Как посмотреть БД удаленно?

#### H2:
Включите H2 Console в коде и настройте firewall:
```bash
sudo ufw allow 8082/tcp
```

#### PostgreSQL:
Настройте удаленный доступ в `postgresql.conf` и `pg_hba.conf`, затем:
```bash
sudo ufw allow 5432/tcp
```

⚠️ **Безопасность**: Не открывайте порты БД в интернет без VPN или SSH туннеля!

### Как создать SSH туннель для безопасного доступа?

```bash
# С локальной машины:
ssh -L 5432:localhost:5432 weatherapp@ваш_vps_ip

# Теперь можете подключиться к localhost:5432 с вашей машины
psql -h localhost -U weatherapp -d weather_db
```

---

**Готово! Теперь вы знаете как работать с базой данных Weather AI Server! 📊**
