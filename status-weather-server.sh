#!/bin/bash

# Weather AI Server - Проверка статуса

echo "╔═══════════════════════════════════════════════════╗"
echo "║      Weather AI Server - Статус                  ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

# Проверка процесса
if ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep > /dev/null; then
    echo "✅ Сервер РАБОТАЕТ"
    echo ""
    
    # Получить PID
    PID=$(ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
    echo "📋 PID: $PID"
    
    # Проверить PID файл
    if [ -f app.pid ]; then
        SAVED_PID=$(cat app.pid)
        echo "📄 PID в файле: $SAVED_PID"
    fi
    
    echo ""
    
    # Health check
    echo "🏥 Health Check:"
    if curl -s -f http://localhost:8080/health > /dev/null; then
        HEALTH=$(curl -s http://localhost:8080/health)
        echo "   Статус: $HEALTH"
        echo "   URL: http://localhost:8080"
    else
        echo "   ⚠️  Сервер запущен, но не отвечает на /health"
    fi
    
    echo ""
    
    # Последние записи
    echo "📊 Последние записи в БД:"
    LATEST=$(curl -s http://localhost:8080/weather/latest 2>/dev/null | head -c 200)
    if [ -n "$LATEST" ]; then
        echo "   Данные получены ✓"
    else
        echo "   Нет данных или ошибка подключения"
    fi
    
    echo ""
    
    # Использование ресурсов
    echo "💻 Использование ресурсов:"
    ps aux | grep "McpClient-1.0-SNAPSHOT.jar" | grep -v grep | awk '{print "   CPU: " $3 "%, MEM: " $4 "%"}'
    
else
    echo "❌ Сервер НЕ РАБОТАЕТ"
    echo ""
    echo "Для запуска используйте:"
    echo "   ./build-and-run.sh"
    echo "   или"
    echo "   ./run-weather-server.sh"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
