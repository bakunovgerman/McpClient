#!/bin/bash

# Weather AI Server - Скрипт перезапуска

set -e

echo "╔═══════════════════════════════════════════════════╗"
echo "║      Weather AI Server - Перезапуск             ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

# Остановка
echo "🛑 Остановка сервера..."
./stop-weather-server.sh

echo ""
echo "⏳ Ожидание 3 секунды..."
sleep 3

# Запуск
echo ""
echo "🚀 Запуск сервера..."
./run-weather-server.sh
