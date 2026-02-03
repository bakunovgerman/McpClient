#!/bin/bash

# Weather AI Server - Скрипт запуска

set -e

echo "╔═══════════════════════════════════════════════════╗"
echo "║         Weather AI Server - Запуск               ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

# Проверка наличия .env файла
if [ ! -f .env ]; then
    echo "⚠️  Файл .env не найден!"
    echo "📝 Создайте файл .env на основе .env.example:"
    echo "   cp .env.example .env"
    echo "   nano .env"
    echo ""
    echo "🔑 Не забудьте указать ваш OPENROUTER_API_KEY!"
    exit 1
fi

# Загрузка переменных окружения
echo "📋 Загрузка переменных окружения..."
export $(cat .env | grep -v '^#' | xargs)

# Проверка наличия API ключа
if [ -z "$OPENROUTER_API_KEY" ]; then
    echo "❌ OPENROUTER_API_KEY не установлен!"
    echo "📝 Отредактируйте файл .env и укажите ваш API ключ"
    exit 1
fi

echo "✅ API ключ найден"

# Проверка наличия JAR файла
if [ ! -f build/libs/McpClient-1.0-SNAPSHOT.jar ]; then
    echo "📦 JAR файл не найден. Запускаю сборку..."
    ./gradlew clean build
    echo "✅ Сборка завершена"
else
    echo "✅ JAR файл найден"
fi

# Создание директорий
mkdir -p logs
mkdir -p data

echo ""
echo "🚀 Запуск Weather AI Server..."
echo "📊 База данных: $DATABASE_URL"
echo "🌐 Сервер будет доступен на: http://localhost:8080"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 Для остановки сервера:"
echo "   - Нажмите Ctrl+C"
echo "   - Или используйте: ./stop-weather-server.sh"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Запуск сервера и сохранение PID
java -jar build/libs/McpClient-1.0-SNAPSHOT.jar &
PID=$!
echo $PID > app.pid
echo "📝 PID сохранен в app.pid: $PID"

# Ждем завершения
wait $PID
rm -f app.pid
