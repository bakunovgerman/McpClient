# 🐳 Docker развертывание Weather AI Server

## Содержание
- [Простой запуск с Docker](#простой-запуск-с-docker)
- [Развертывание с Docker Compose](#развертывание-с-docker-compose)
- [Развертывание с PostgreSQL](#развертывание-с-postgresql)
- [Управление контейнерами](#управление-контейнерами)
- [Мониторинг](#мониторинг)
- [Резервное копирование](#резервное-копирование)

---

## Простой запуск с Docker

### 1. Создание Docker образа

```bash
# Сборка образа
docker build -t weather-ai-server .

# Проверка образа
docker images | grep weather-ai-server
```

### 2. Запуск контейнера

```bash
# Базовый запуск (с H2 database)
docker run -d \
  --name weather-ai \
  -p 8080:8080 \
  -e OPENROUTER_API_KEY="ваш_ключ" \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  weather-ai-server
```

### 3. Проверка работы

```bash
# Проверка логов
docker logs -f weather-ai

# Health check
curl http://localhost:8080/health

# Получение последних записей
curl http://localhost:8080/weather/latest
```

### 4. Остановка и удаление

```bash
# Остановка
docker stop weather-ai

# Запуск
docker start weather-ai

# Перезапуск
docker restart weather-ai

# Удаление
docker rm -f weather-ai
```

---

## Развертывание с Docker Compose

### 1. Создание .env файла

```bash
# Создайте файл .env
cat > .env << 'EOF'
OPENROUTER_API_KEY=ваш_ключ_от_openrouter
DATABASE_URL=jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE
EOF
```

### 2. Запуск с Docker Compose

```bash
# Сборка и запуск
docker-compose up -d

# Только сборка
docker-compose build

# Запуск без пересборки
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Просмотр логов только weather-ai
docker-compose logs -f weather-ai
```

### 3. Проверка статуса

```bash
# Статус сервисов
docker-compose ps

# Проверка health
docker-compose exec weather-ai wget -qO- http://localhost:8080/health
```

### 4. Остановка

```bash
# Остановка всех сервисов
docker-compose down

# Остановка с удалением volumes
docker-compose down -v

# Остановка с удалением images
docker-compose down --rmi all
```

---

## Развертывание с PostgreSQL

### 1. Обновление .env для PostgreSQL

```bash
cat > .env << 'EOF'
OPENROUTER_API_KEY=ваш_ключ_от_openrouter
DATABASE_URL=jdbc:postgresql://postgres:5432/weather_db?user=weatherapp&password=changeme
POSTGRES_PASSWORD=changeme
EOF
```

### 2. Запуск с PostgreSQL

```bash
# Запуск обоих сервисов (weather-ai + postgres)
docker-compose up -d

# Проверка, что PostgreSQL готов
docker-compose logs postgres | grep "database system is ready"

# Проверка подключения к БД
docker-compose exec postgres psql -U weatherapp -d weather_db -c "SELECT version();"
```

### 3. Просмотр данных в PostgreSQL

```bash
# Подключение к PostgreSQL
docker-compose exec postgres psql -U weatherapp -d weather_db

# SQL команды:
\dt                                    -- список таблиц
SELECT * FROM weather_records LIMIT 10; -- последние записи
SELECT COUNT(*) FROM weather_records;   -- количество записей
\q                                     -- выход
```

### 4. Резервное копирование PostgreSQL

```bash
# Создание бэкапа
docker-compose exec postgres pg_dump -U weatherapp weather_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Восстановление
docker-compose exec -T postgres psql -U weatherapp weather_db < backup_20260203_101530.sql
```

---

## Управление контейнерами

### Просмотр информации

```bash
# Статус контейнеров
docker-compose ps

# Использование ресурсов
docker stats weather-ai-server

# Инспекция контейнера
docker inspect weather-ai-server

# Проверка сети
docker network inspect mcpclient_weather-network
```

### Выполнение команд в контейнере

```bash
# Shell в контейнере
docker-compose exec weather-ai sh

# Просмотр файлов
docker-compose exec weather-ai ls -la /app

# Проверка Java версии
docker-compose exec weather-ai java -version

# Просмотр логов внутри контейнера
docker-compose exec weather-ai cat /app/logs/weather-agent.log
```

### Обновление приложения

```bash
# Получить последний код
git pull

# Пересобрать и перезапустить
docker-compose up -d --build

# Или пошагово:
docker-compose build
docker-compose down
docker-compose up -d
```

---

## Мониторинг

### Логи

```bash
# Все логи
docker-compose logs -f

# Только weather-ai
docker-compose logs -f weather-ai

# Только postgres
docker-compose logs -f postgres

# Последние 100 строк
docker-compose logs --tail=100 weather-ai

# С временными метками
docker-compose logs -f -t weather-ai
```

### Healthcheck

```bash
# Проверка health через Docker
docker inspect --format='{{json .State.Health}}' weather-ai-server | jq

# Проверка через curl
curl http://localhost:8080/health

# Автоматический мониторинг
watch -n 5 'curl -s http://localhost:8080/health'
```

### Использование ресурсов

```bash
# CPU и память в реальном времени
docker stats weather-ai-server

# Размер контейнера
docker-compose exec weather-ai du -sh /app

# Размер volumes
docker volume ls
docker volume inspect mcpclient_postgres_data
```

---

## Резервное копирование

### Backup данных H2

```bash
# Создание архива директории data
tar -czf backup_h2_$(date +%Y%m%d_%H%M%S).tar.gz data/

# Восстановление
tar -xzf backup_h2_20260203_101530.tar.gz
```

### Backup PostgreSQL

```bash
# Полный бэкап
docker-compose exec postgres pg_dump -U weatherapp weather_db > backup.sql

# Бэкап с сжатием
docker-compose exec postgres pg_dump -U weatherapp weather_db | gzip > backup_$(date +%Y%m%d).sql.gz

# Восстановление
gunzip < backup_20260203.sql.gz | docker-compose exec -T postgres psql -U weatherapp weather_db
```

### Автоматический backup (cron)

```bash
# Создаем скрипт backup
cat > backup_docker.sh << 'EOF'
#!/bin/bash
BACKUP_DIR=~/backups
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# Backup PostgreSQL
docker-compose exec -T postgres pg_dump -U weatherapp weather_db | gzip > $BACKUP_DIR/weather_backup_$TIMESTAMP.sql.gz

# Удаляем старые бэкапы (>30 дней)
find $BACKUP_DIR -name "weather_backup_*.sql.gz" -mtime +30 -delete

echo "Backup completed: weather_backup_$TIMESTAMP.sql.gz"
EOF

chmod +x backup_docker.sh

# Добавляем в cron
crontab -e
# Добавляем строку (каждый день в 3:00):
# 0 3 * * * /path/to/backup_docker.sh
```

---

## Развертывание на VPS с Docker

### 1. Установка Docker на VPS

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Добавляем пользователя в группу docker
sudo usermod -aG docker $USER

# Установка Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Проверка
docker --version
docker-compose --version
```

### 2. Клонирование и запуск

```bash
# Клонируем репозиторий
git clone <repository-url>
cd McpClient

# Создаем .env файл
nano .env
# Добавляем OPENROUTER_API_KEY

# Запускаем
docker-compose up -d

# Проверяем
docker-compose logs -f
```

### 3. Настройка Nginx (reverse proxy)

```bash
# Устанавливаем Nginx
sudo apt install nginx -y

# Создаем конфигурацию
sudo nano /etc/nginx/sites-available/weather-ai

# Содержимое:
server {
    listen 80;
    server_name ваш_домен.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Активируем
sudo ln -s /etc/nginx/sites-available/weather-ai /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 4. Автозапуск при старте системы

Docker Compose контейнеры с `restart: unless-stopped` автоматически запускаются при перезагрузке системы.

```bash
# Проверяем, что Docker запускается при старте
sudo systemctl enable docker

# Перезагружаем для проверки
sudo reboot

# После перезагрузки проверяем
docker-compose ps
```

---

## Полезные команды

### Очистка

```bash
# Остановить все контейнеры
docker-compose down

# Удалить образы
docker-compose down --rmi all

# Удалить volumes
docker-compose down -v

# Полная очистка Docker системы
docker system prune -a --volumes
```

### Debugging

```bash
# Проверка логов с ошибками
docker-compose logs weather-ai | grep ERROR

# Проверка переменных окружения
docker-compose exec weather-ai env | grep OPENROUTER

# Проверка сети
docker network inspect mcpclient_weather-network

# Проверка портов
docker-compose port weather-ai 8080
```

### Production настройки

```bash
# Увеличение лимита памяти
docker-compose.yml:
  weather-ai:
    ...
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

# Настройка логирования
docker-compose.yml:
  weather-ai:
    ...
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

---

## Решение проблем

### Контейнер не запускается

```bash
# Проверяем логи
docker-compose logs weather-ai

# Проверяем статус
docker-compose ps

# Проверяем healthcheck
docker inspect weather-ai-server | grep Health -A 10
```

### Проблемы с подключением к БД

```bash
# Проверяем, что PostgreSQL запущен
docker-compose ps postgres

# Проверяем логи PostgreSQL
docker-compose logs postgres

# Тестируем подключение
docker-compose exec postgres psql -U weatherapp -d weather_db -c "SELECT 1;"
```

### Нет доступа к API

```bash
# Проверяем порты
docker-compose ps
docker-compose port weather-ai 8080

# Проверяем firewall
sudo ufw status
sudo ufw allow 8080/tcp

# Проверяем Nginx (если используется)
sudo nginx -t
sudo systemctl status nginx
```

---

## Мультистейдж сборка (оптимизация)

Наш Dockerfile уже использует multistage build для минимизации размера образа:

```dockerfile
# Этап 1: Сборка (использует полный Gradle + JDK)
FROM gradle:8.5-jdk17 AS build
...

# Этап 2: Запуск (использует только JRE, меньший размер)
FROM eclipse-temurin:17-jre-alpine
...
```

Размер итогового образа: ~200 MB (вместо ~700 MB с полным JDK)

---

**Готово! Ваш Weather AI Server готов к запуску в Docker! 🐳**
