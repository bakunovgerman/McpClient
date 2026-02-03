# 📋 Шпаргалка команд Weather AI Server

Быстрый справочник всех команд для управления сервером.

---

## 🚀 Управление сервером

### Запуск

```bash
# Полная сборка и запуск (первый раз)
./build-and-run.sh

# Быстрый запуск (если уже собран)
./run-weather-server.sh

# Запуск в фоне
nohup ./run-weather-server.sh > /dev/null 2>&1 &
```

### Остановка

```bash
# Если запущен в терминале
Ctrl+C

# Если запущен в фоне
./stop-weather-server.sh

# Вручную
kill $(cat app.pid)

# Принудительно
kill -9 $(cat app.pid)
```

### Перезапуск

```bash
# Автоматический перезапуск
./restart-weather-server.sh

# Вручную
./stop-weather-server.sh && sleep 2 && ./run-weather-server.sh
```

### Статус

```bash
# Проверка статуса
./status-weather-server.sh

# Вручную
ps aux | grep "McpClient-1.0-SNAPSHOT.jar"
```

---

## 🔨 Сборка

```bash
# Полная сборка
./gradlew clean build

# Только сборка (без тестов)
./gradlew clean build -x test

# Проверить версии
./gradlew --version
java -version
```

---

## 🌐 API запросы

### Health Check

```bash
# Простая проверка
curl http://localhost:8080/health

# С выводом статус кода
curl -w "\nHTTP Code: %{http_code}\n" http://localhost:8080/health
```

### Получение данных

```bash
# Последние 10 записей
curl http://localhost:8080/weather/latest

# С форматированием (jq)
curl -s http://localhost:8080/weather/latest | jq '.'

# Только текст погоды
curl -s http://localhost:8080/weather/latest | jq '.[0].weatherResponse'

# Все записи
curl http://localhost:8080/weather/all

# Количество записей
curl -s http://localhost:8080/weather/all | jq 'length'
```

### Ручные запросы

```bash
# Запросить погоду немедленно
curl http://localhost:8080/weather/check-now

# Проверить, что запрос обработался
sleep 5 && curl -s http://localhost:8080/weather/latest | jq '.[0]'
```

---

## 📝 Логи

### Просмотр

```bash
# В реальном времени
tail -f logs/weather-agent.log

# Последние 100 строк
tail -n 100 logs/weather-agent.log

# С подсветкой (grep --color)
tail -f logs/weather-agent.log | grep --color -E "ERROR|WARN|INFO"
```

### Поиск

```bash
# Поиск ошибок
grep ERROR logs/weather-agent.log

# Поиск ответов LLM
grep "Ответ LLM" logs/weather-agent.log

# Поиск операций с БД
grep "База данных" logs/weather-agent.log

# За сегодня
grep "$(date +%Y-%m-%d)" logs/weather-agent.log
```

### Очистка

```bash
# Очистить логи
rm -rf logs/*.log

# Очистить старые (>7 дней)
find logs/ -name "*.log" -mtime +7 -delete
```

---

## 🗄️ База данных (H2)

### Просмотр файлов

```bash
# Список файлов БД
ls -lh data/

# Размер БД
du -sh data/
```

### Резервное копирование

```bash
# Создать backup
tar -czf backup_$(date +%Y%m%d_%H%M%S).tar.gz data/

# Восстановить
tar -xzf backup_20260203_101530.tar.gz
```

### Очистка

```bash
# Удалить БД (ОСТОРОЖНО!)
rm -rf data/

# Пересоздать (запустите сервер, он создаст новую)
./run-weather-server.sh
```

---

## 🐳 Docker

### Сборка

```bash
# Собрать образ
docker build -t weather-ai-server .

# Проверить образ
docker images | grep weather-ai
```

### Запуск

```bash
# С Docker Compose
docker-compose up -d

# Без Compose
docker run -d \
  --name weather-ai \
  -p 8080:8080 \
  -e OPENROUTER_API_KEY="ваш_ключ" \
  -v $(pwd)/data:/app/data \
  weather-ai-server
```

### Управление

```bash
# Остановка
docker-compose down

# Перезапуск
docker-compose restart

# Логи
docker-compose logs -f weather-ai

# Статус
docker-compose ps

# Войти в контейнер
docker-compose exec weather-ai sh
```

---

## 🖥️ Systemd (VPS)

### Управление

```bash
# Запуск
sudo systemctl start weather-ai

# Остановка
sudo systemctl stop weather-ai

# Перезапуск
sudo systemctl restart weather-ai

# Статус
sudo systemctl status weather-ai

# Логи
sudo journalctl -u weather-ai -f

# Автозапуск
sudo systemctl enable weather-ai

# Отключить автозапуск
sudo systemctl disable weather-ai
```

---

## 🔍 Диагностика

### Процессы

```bash
# Найти процесс сервера
ps aux | grep "McpClient-1.0-SNAPSHOT.jar"

# Все Java процессы
ps aux | grep java

# Использование ресурсов
top -p $(cat app.pid)
```

### Порты

```bash
# Проверить порт 8080
lsof -i :8080

# Netstat
netstat -an | grep 8080

# SS (современная альтернатива)
ss -tulpn | grep 8080
```

### Сеть

```bash
# Проверить доступность
curl -I http://localhost:8080/health

# С таймаутом
curl --max-time 5 http://localhost:8080/health

# Traceroute (для VPS)
traceroute ваш-домен.com
```

---

## 🔧 Конфигурация

### Переменные окружения

```bash
# Показать
cat .env

# Редактировать
nano .env

# Экспортировать вручную
export OPENROUTER_API_KEY="ваш_ключ"
export DATABASE_URL="jdbc:h2:./data/weather_db"
```

### Проверка

```bash
# Проверить API ключ
echo $OPENROUTER_API_KEY

# Проверить DATABASE_URL
echo $DATABASE_URL

# Проверить Java
java -version

# Проверить Gradle
./gradlew --version
```

---

## 📊 Мониторинг

### Автоматическая проверка

```bash
# Скрипт мониторинга (каждые 30 секунд)
watch -n 30 './status-weather-server.sh'

# Проверка uptime
watch -n 60 'curl -s http://localhost:8080/health || echo "DOWN"'
```

### Алерты

```bash
# Email при падении (требует mailutils)
while true; do
  if ! curl -s -f http://localhost:8080/health > /dev/null; then
    echo "Сервер не отвечает!" | mail -s "Weather AI Alert" your@email.com
  fi
  sleep 300
done &
```

---

## 🧹 Очистка

### Временные файлы

```bash
# Gradle cache
./gradlew clean

# Все временные файлы
rm -rf build/ .gradle/ logs/*.log
```

### Docker

```bash
# Остановить и удалить контейнеры
docker-compose down -v

# Удалить образы
docker rmi weather-ai-server

# Полная очистка Docker
docker system prune -a --volumes
```

---

## 🆘 Экстренные команды

### Сервер завис

```bash
# 1. Попытка нормальной остановки
./stop-weather-server.sh

# 2. Принудительная остановка
kill -9 $(ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')

# 3. Убить все Java
killall -9 java
```

### Порт занят

```bash
# Найти процесс
lsof -i :8080

# Убить процесс
kill -9 $(lsof -t -i:8080)
```

### Нет места на диске

```bash
# Проверить место
df -h

# Очистить логи
rm -rf logs/*.log

# Очистить старую БД
rm -rf data/weather_db.*.old
```

---

## 📦 Быстрые команды

Копируйте и используйте эти однострочники:

```bash
# Полный перезапуск с пересборкой
./stop-weather-server.sh && ./gradlew clean build && ./run-weather-server.sh

# Проверка всего
./status-weather-server.sh && curl -s http://localhost:8080/weather/latest | jq '.[0]'

# Мониторинг логов с цветом
tail -f logs/weather-agent.log | grep --color=always -E "ERROR|WARN|INFO|$"

# Статистика БД
echo "Записей в БД: $(curl -s http://localhost:8080/weather/all | jq 'length')"

# Backup всего
tar -czf full_backup_$(date +%Y%m%d_%H%M%S).tar.gz data/ logs/ .env

# Health check loop
while true; do curl -s http://localhost:8080/health && echo " - $(date)"; sleep 10; done
```

---

## 🔗 Полезные ссылки

- **START_HERE_WEATHER.md** - Начните здесь
- **HOW_TO_STOP.md** - Подробно об остановке
- **README_WEATHER_SERVER.md** - Полная документация
- **FAQ.md** - Частые вопросы
- **TEST_API.md** - Тестирование

---

**Сохраните эту шпаргалку для быстрого доступа! 📋**
