# 🚀 Развертывание Weather AI Server на VPS

## Содержание
- [Требования](#требования)
- [Подготовка VPS](#подготовка-vps)
- [Установка зависимостей](#установка-зависимостей)
- [Сборка проекта](#сборка-проекта)
- [Настройка переменных окружения](#настройка-переменных-окружения)
- [Запуск сервера](#запуск-сервера)
- [Настройка systemd сервиса](#настройка-systemd-сервиса)
- [Настройка PostgreSQL](#настройка-postgresql)
- [Настройка Nginx](#настройка-nginx)
- [Мониторинг и логи](#мониторинг-и-логи)

---

## Требования

### Минимальные требования VPS:
- **CPU**: 1 core
- **RAM**: 1 GB
- **Disk**: 10 GB
- **OS**: Ubuntu 20.04+ / Debian 11+ / CentOS 8+

### Необходимое ПО:
- Java 17+
- Gradle 8.0+ (или использовать Gradle Wrapper)
- Git (для клонирования репозитория)
- PostgreSQL 14+ (опционально, для production)

---

## Подготовка VPS

### 1. Подключение к VPS

```bash
ssh root@ВАШ_IP_АДРЕС
```

### 2. Обновление системы

```bash
# Для Ubuntu/Debian
sudo apt update && sudo apt upgrade -y

# Для CentOS/RHEL
sudo yum update -y
```

### 3. Создание пользователя для приложения

```bash
# Создаем пользователя
sudo useradd -m -s /bin/bash weatherapp

# Добавляем в группу sudo (если нужно)
sudo usermod -aG sudo weatherapp

# Переключаемся на пользователя
sudo su - weatherapp
```

---

## Установка зависимостей

### 1. Установка Java 17

```bash
# Для Ubuntu/Debian
sudo apt install openjdk-17-jdk -y

# Для CentOS/RHEL
sudo yum install java-17-openjdk-devel -y

# Проверка версии
java -version
```

### 2. Установка Git

```bash
# Для Ubuntu/Debian
sudo apt install git -y

# Для CentOS/RHEL
sudo yum install git -y
```

---

## Сборка проекта

### 1. Клонирование репозитория

```bash
cd ~
git clone https://github.com/YOUR_USERNAME/weather-ai-server.git
cd weather-ai-server
```

### 2. Сборка JAR файла

```bash
# Используем Gradle Wrapper (не требует установки Gradle)
./gradlew clean build

# JAR файл будет создан в build/libs/McpClient-1.0-SNAPSHOT.jar
```

### 3. Проверка сборки

```bash
ls -lh build/libs/
# Должен быть файл McpClient-1.0-SNAPSHOT.jar
```

---

## Настройка переменных окружения

### 1. Создание файла с переменными

```bash
# Создаем директорию для конфигурации
mkdir -p ~/weather-ai-server/config

# Создаем файл с переменными окружения
nano ~/weather-ai-server/config/env.conf
```

### 2. Содержимое env.conf

```bash
# OpenRouter API Key (обязательно!)
OPENROUTER_API_KEY=ваш_ключ_от_openrouter

# Database URL (по умолчанию H2, для PostgreSQL см. ниже)
DATABASE_URL=jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE

# Для PostgreSQL:
# DATABASE_URL=jdbc:postgresql://localhost:5432/weather_db?user=weatherapp&password=ваш_пароль
```

### 3. Получение OpenRouter API Key

1. Зарегистрируйтесь на https://openrouter.ai/
2. Перейдите в раздел "Keys" в личном кабинете
3. Создайте новый API ключ
4. Скопируйте ключ и вставьте в `env.conf`

---

## Запуск сервера

### Вариант 1: Простой запуск (для тестирования)

```bash
# Экспортируем переменные
export OPENROUTER_API_KEY="ваш_ключ"
export DATABASE_URL="jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE"

# Запускаем сервер
java -jar build/libs/McpClient-1.0-SNAPSHOT.jar
```

### Вариант 2: Запуск в фоновом режиме с nohup

```bash
# Создаем скрипт запуска
cat > ~/weather-ai-server/start.sh << 'EOF'
#!/bin/bash
cd ~/weather-ai-server
source config/env.conf
nohup java -jar build/libs/McpClient-1.0-SNAPSHOT.jar > logs/app.log 2>&1 &
echo $! > app.pid
echo "Server started with PID: $(cat app.pid)"
EOF

# Делаем скрипт исполняемым
chmod +x ~/weather-ai-server/start.sh

# Запускаем
./start.sh
```

### Вариант 3: Остановка сервера

```bash
# Создаем скрипт остановки
cat > ~/weather-ai-server/stop.sh << 'EOF'
#!/bin/bash
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    kill $PID
    rm app.pid
    echo "Server stopped (PID: $PID)"
else
    echo "PID file not found"
fi
EOF

chmod +x ~/weather-ai-server/stop.sh

# Останавливаем
./stop.sh
```

---

## Настройка systemd сервиса

### 1. Создание systemd unit файла

```bash
sudo nano /etc/systemd/system/weather-ai.service
```

### 2. Содержимое weather-ai.service

```ini
[Unit]
Description=Weather AI Server
After=network.target

[Service]
Type=simple
User=weatherapp
WorkingDirectory=/home/weatherapp/weather-ai-server
EnvironmentFile=/home/weatherapp/weather-ai-server/config/env.conf
ExecStart=/usr/bin/java -jar /home/weatherapp/weather-ai-server/build/libs/McpClient-1.0-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/weatherapp/weather-ai-server/logs/app.log
StandardError=append:/home/weatherapp/weather-ai-server/logs/error.log

[Install]
WantedBy=multi-user.target
```

### 3. Управление сервисом

```bash
# Перезагрузка systemd
sudo systemctl daemon-reload

# Запуск сервиса
sudo systemctl start weather-ai

# Автозапуск при старте системы
sudo systemctl enable weather-ai

# Проверка статуса
sudo systemctl status weather-ai

# Остановка сервиса
sudo systemctl stop weather-ai

# Перезапуск сервиса
sudo systemctl restart weather-ai

# Просмотр логов
sudo journalctl -u weather-ai -f
```

---

## Настройка PostgreSQL

### 1. Установка PostgreSQL

```bash
# Для Ubuntu/Debian
sudo apt install postgresql postgresql-contrib -y

# Для CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib -y
sudo postgresql-setup initdb
```

### 2. Создание базы данных и пользователя

```bash
# Переключаемся на пользователя postgres
sudo -u postgres psql

# В консоли PostgreSQL выполняем:
CREATE DATABASE weather_db;
CREATE USER weatherapp WITH ENCRYPTED PASSWORD 'ваш_надежный_пароль';
GRANT ALL PRIVILEGES ON DATABASE weather_db TO weatherapp;
\q
```

### 3. Настройка доступа

```bash
# Редактируем pg_hba.conf
sudo nano /etc/postgresql/14/main/pg_hba.conf

# Добавляем строку (замените 14 на вашу версию PostgreSQL):
# local   weather_db      weatherapp                              md5
```

### 4. Перезапуск PostgreSQL

```bash
sudo systemctl restart postgresql
```

### 5. Обновление DATABASE_URL

```bash
nano ~/weather-ai-server/config/env.conf

# Измените DATABASE_URL на:
DATABASE_URL=jdbc:postgresql://localhost:5432/weather_db?user=weatherapp&password=ваш_пароль
```

---

## Настройка Nginx

### 1. Установка Nginx

```bash
# Для Ubuntu/Debian
sudo apt install nginx -y

# Для CentOS/RHEL
sudo yum install nginx -y
```

### 2. Создание конфигурации

```bash
sudo nano /etc/nginx/sites-available/weather-ai
```

### 3. Содержимое конфигурации

```nginx
server {
    listen 80;
    server_name ваш_домен.com;  # или IP адрес

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 4. Активация конфигурации

```bash
# Создаем символическую ссылку
sudo ln -s /etc/nginx/sites-available/weather-ai /etc/nginx/sites-enabled/

# Проверяем конфигурацию
sudo nginx -t

# Перезапускаем Nginx
sudo systemctl restart nginx
```

### 5. Настройка SSL (опционально)

```bash
# Установка Certbot
sudo apt install certbot python3-certbot-nginx -y

# Получение сертификата
sudo certbot --nginx -d ваш_домен.com

# Автообновление сертификата
sudo systemctl enable certbot.timer
```

---

## Мониторинг и логи

### 1. Просмотр логов приложения

```bash
# Логи через systemd
sudo journalctl -u weather-ai -f

# Логи из файла
tail -f ~/weather-ai-server/logs/weather-agent.log

# Логи приложения
tail -f ~/weather-ai-server/logs/app.log

# Логи ошибок
tail -f ~/weather-ai-server/logs/error.log
```

### 2. Мониторинг использования ресурсов

```bash
# CPU и память
top
# или
htop

# Статус сервиса
sudo systemctl status weather-ai

# Проверка портов
sudo netstat -tulpn | grep 8080
# или
sudo ss -tulpn | grep 8080
```

### 3. Проверка работоспособности

```bash
# Проверка health endpoint
curl http://localhost:8080/health

# Получение последних записей о погоде
curl http://localhost:8080/weather/latest

# Ручной запрос погоды
curl http://localhost:8080/weather/check-now
```

### 4. Ротация логов

```bash
# Создаем конфигурацию logrotate
sudo nano /etc/logrotate.d/weather-ai
```

Содержимое:

```
/home/weatherapp/weather-ai-server/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 weatherapp weatherapp
}
```

---

## Полезные команды

### Обновление приложения

```bash
cd ~/weather-ai-server
git pull
./gradlew clean build
sudo systemctl restart weather-ai
```

### Резервное копирование БД (H2)

```bash
# Остановить сервис
sudo systemctl stop weather-ai

# Скопировать файл БД
cp -r ~/weather-ai-server/data ~/weather-ai-server/data_backup_$(date +%Y%m%d)

# Запустить сервис
sudo systemctl start weather-ai
```

### Резервное копирование БД (PostgreSQL)

```bash
# Создание бэкапа
pg_dump -U weatherapp weather_db > ~/backup_weather_$(date +%Y%m%d).sql

# Восстановление из бэкапа
psql -U weatherapp weather_db < ~/backup_weather_20260203.sql
```

---

## Решение проблем

### Сервер не запускается

```bash
# Проверяем логи
sudo journalctl -u weather-ai -n 50

# Проверяем переменные окружения
cat ~/weather-ai-server/config/env.conf

# Проверяем наличие OPENROUTER_API_KEY
grep OPENROUTER_API_KEY ~/weather-ai-server/config/env.conf
```

### Порт 8080 занят

```bash
# Находим процесс
sudo lsof -i :8080

# Убиваем процесс
sudo kill -9 PID
```

### Проблемы с правами доступа

```bash
# Устанавливаем правильного владельца
sudo chown -R weatherapp:weatherapp ~/weather-ai-server

# Проверяем права
ls -la ~/weather-ai-server
```

---

## Контакты и поддержка

При возникновении проблем:
1. Проверьте логи: `sudo journalctl -u weather-ai -f`
2. Проверьте статус: `sudo systemctl status weather-ai`
3. Проверьте переменные окружения
4. Убедитесь, что OpenRouter API ключ валиден

---

**Готово! Ваш Weather AI Server успешно развернут на VPS! 🎉**
