#!/bin/bash

# Weather AI Server - Полная сборка и запуск

set -e

echo "╔═══════════════════════════════════════════════════╗"
echo "║    Weather AI Server - Сборка и Запуск           ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

# Проверка Java
if ! command -v java &> /dev/null; then
    echo "❌ Java не установлена!"
    echo "📦 Установите Java 17 или выше"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep version | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Требуется Java 17 или выше. Текущая версия: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java версия: $(java -version 2>&1 | head -n 1)"

# Проверка .env файла
if [ ! -f .env ]; then
    echo ""
    echo "⚠️  Файл .env не найден!"
    echo "📝 Создаю .env из .env.example..."
    cp .env.example .env
    echo "✅ Файл .env создан"
    echo ""
    echo "🔑 ВАЖНО: Отредактируйте .env и укажите ваш OPENROUTER_API_KEY:"
    echo "   nano .env"
    echo ""
    read -p "Нажмите Enter после редактирования .env..."
fi

# Загрузка переменных
export $(cat .env | grep -v '^#' | xargs)

if [ -z "$OPENROUTER_API_KEY" ] || [ "$OPENROUTER_API_KEY" = "your_openrouter_api_key_here" ]; then
    echo "❌ OPENROUTER_API_KEY не настроен!"
    echo "📝 Отредактируйте .env и укажите ваш API ключ от https://openrouter.ai/"
    exit 1
fi

# Очистка и сборка
echo ""
echo "🧹 Очистка предыдущих сборок..."
./gradlew clean

echo ""
echo "🔨 Сборка проекта..."
./gradlew build

if [ $? -ne 0 ]; then
    echo "❌ Ошибка при сборке проекта"
    exit 1
fi

echo ""
echo "✅ Сборка успешно завершена!"

# Создание необходимых директорий
mkdir -p logs
mkdir -p data

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Запуск Weather AI Server..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📊 База данных: $DATABASE_URL"
echo "🌐 API: http://localhost:8080"
echo "🏥 Health check: http://localhost:8080/health"
echo "🌤️  Последние записи: http://localhost:8080/weather/latest"
echo ""
echo "Нажмите Ctrl+C для остановки сервера"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Запуск
java -jar build/libs/McpClient-1.0-SNAPSHOT.jar
