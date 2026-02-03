# 🛑 Как остановить Weather AI Server

## Способы остановки сервера

### Способ 1: Ctrl+C (если запущен в терминале)

Если вы запустили сервер через `./build-and-run.sh` или `./run-weather-server.sh` и видите логи в терминале:

```bash
# Просто нажмите:
Ctrl + C
```

Сервер получит сигнал SIGTERM и корректно остановится.

---

### Способ 2: Скрипт остановки

Если сервер запущен в фоновом режиме:

```bash
# Используйте скрипт остановки
./stop-weather-server.sh
```

Этот скрипт:
- Найдет процесс сервера
- Отправит сигнал остановки
- Подождет корректного завершения
- При необходимости выполнит принудительную остановку

---

### Способ 3: Systemd (на VPS)

Если сервер установлен как systemd сервис:

```bash
# Остановка
sudo systemctl stop weather-ai

# Проверка статуса
sudo systemctl status weather-ai

# Отключить автозапуск (опционально)
sudo systemctl disable weather-ai
```

---

### Способ 4: Docker

Если сервер запущен в Docker:

```bash
# С Docker Compose
docker-compose down

# Или только остановка (без удаления)
docker-compose stop

# Plain Docker
docker stop weather-ai-server

# Удалить контейнер
docker rm weather-ai-server
```

---

### Способ 5: Найти и убить процесс вручную

```bash
# 1. Найти процесс
ps aux | grep "McpClient-1.0-SNAPSHOT.jar"

# 2. Запомнить PID (второй столбец)

# 3. Остановить процесс
kill PID

# Или принудительно
kill -9 PID
```

Пример:

```bash
# Найти процесс
$ ps aux | grep "McpClient-1.0-SNAPSHOT.jar"
user  12345  0.5  2.1  ....  java -jar build/libs/McpClient-1.0-SNAPSHOT.jar

# Остановить (используйте PID из второго столбца)
$ kill 12345
```

---

### Способ 6: Остановить все Java процессы (крайний случай)

⚠️ **ВНИМАНИЕ**: Это остановит ВСЕ Java приложения!

```bash
# Найти все Java процессы
ps aux | grep java

# Остановить все Java процессы
killall java
```

---

## Проверка, что сервер остановлен

### Проверить процесс

```bash
# Проверить, запущен ли процесс
ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep

# Если ничего не выводится - сервер остановлен
```

### Проверить порт

```bash
# Проверить, занят ли порт 8080
lsof -i :8080

# Или
netstat -an | grep 8080

# Если ничего не выводится - порт свободен
```

### Проверить через API

```bash
# Попытка подключения
curl http://localhost:8080/health

# Если ошибка "Connection refused" - сервер остановлен
```

---

## Автоматическая остановка

### Остановка через N секунд

```bash
# Создать скрипт
cat > auto-stop.sh << 'EOF'
#!/bin/bash
SECONDS=$1
echo "Сервер будет остановлен через $SECONDS секунд..."
sleep $SECONDS
./stop-weather-server.sh
EOF

chmod +x auto-stop.sh

# Использование (например, через 60 секунд)
./auto-stop.sh 60 &
```

### Остановка в определенное время (cron)

```bash
# Добавить в crontab
crontab -e

# Остановка каждый день в 23:00
0 23 * * * /path/to/McpClient/stop-weather-server.sh
```

---

## Graceful Shutdown

### Что происходит при остановке?

1. **Сервер получает SIGTERM** (Ctrl+C или kill)
2. **Ktor завершает обработку** текущих запросов
3. **Закрываются соединения** с БД
4. **Закрывается HTTP клиент** OpenRouter
5. **Сохраняются логи**
6. **Процесс завершается**

### Проверить логи остановки

```bash
# Последние строки логов
tail -n 50 logs/weather-agent.log

# Поиск сообщений об остановке
grep -i "shutdown\|stop\|exit" logs/weather-agent.log
```

---

## Troubleshooting

### Сервер не останавливается

```bash
# 1. Проверить, действительно ли запущен
ps aux | grep "McpClient-1.0-SNAPSHOT.jar"

# 2. Попробовать SIGTERM
kill PID

# 3. Подождать 10 секунд
sleep 10

# 4. Если все еще работает - SIGKILL
kill -9 PID
```

### Несколько экземпляров сервера

```bash
# Найти все
ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep

# Остановить все
pkill -f "McpClient-1.0-SNAPSHOT.jar"
```

### Порт 8080 остается занятым

```bash
# Найти процесс, занимающий порт
lsof -i :8080

# Убить его
kill -9 PID
```

---

## Резюме команд

| Сценарий | Команда |
|----------|---------|
| Запущен в терминале | `Ctrl+C` |
| Фоновый режим | `./stop-weather-server.sh` |
| Systemd (VPS) | `sudo systemctl stop weather-ai` |
| Docker Compose | `docker-compose down` |
| Docker контейнер | `docker stop weather-ai-server` |
| Вручную | `kill PID` |
| Принудительно | `kill -9 PID` |

---

## Скрипты для управления

### Создать полный набор скриптов

```bash
# start.sh
./build-and-run.sh

# stop.sh
./stop-weather-server.sh

# restart.sh
./stop-weather-server.sh && sleep 2 && ./run-weather-server.sh

# status.sh
if ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep > /dev/null; then
    echo "✅ Сервер работает"
    curl -s http://localhost:8080/health
else
    echo "❌ Сервер не запущен"
fi
```

---

**Теперь вы знаете все способы остановки сервера! 🛑**
