#!/bin/bash

# Weather AI Server - Скрипт остановки

set -e

echo "╔═══════════════════════════════════════════════════╗"
echo "║      Weather AI Server - Остановка               ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

# Проверка PID файла
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    echo "📋 Найден PID файл: $PID"
    
    # Проверяем, запущен ли процесс
    if ps -p $PID > /dev/null 2>&1; then
        echo "🛑 Остановка сервера (PID: $PID)..."
        kill $PID
        
        # Ждем остановки (максимум 10 секунд)
        for i in {1..10}; do
            if ! ps -p $PID > /dev/null 2>&1; then
                echo "✅ Сервер успешно остановлен"
                rm app.pid
                exit 0
            fi
            sleep 1
        done
        
        # Если не остановился, принудительная остановка
        echo "⚠️  Сервер не остановился, принудительная остановка..."
        kill -9 $PID
        rm app.pid
        echo "✅ Сервер принудительно остановлен"
    else
        echo "⚠️  Процесс с PID $PID не найден (уже остановлен?)"
        rm app.pid
    fi
else
    echo "⚠️  PID файл не найден. Попытка найти процесс вручную..."
    
    # Ищем процесс Java с нашим JAR
    PID=$(ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
    
    if [ -n "$PID" ]; then
        echo "📋 Найден процесс: $PID"
        echo "🛑 Остановка сервера..."
        kill $PID
        sleep 2
        
        if ! ps -p $PID > /dev/null 2>&1; then
            echo "✅ Сервер успешно остановлен"
        else
            echo "⚠️  Принудительная остановка..."
            kill -9 $PID
            echo "✅ Сервер принудительно остановлен"
        fi
    else
        echo "❌ Процесс Weather AI Server не найден"
        echo "   Возможно, сервер уже остановлен"
    fi
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Готово!"
