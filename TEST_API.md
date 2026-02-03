# 🧪 Тестирование Weather AI Server API

## Содержание
- [Запуск сервера](#запуск-сервера)
- [Тестирование endpoints](#тестирование-endpoints)
- [Примеры использования](#примеры-использования)
- [Мониторинг работы](#мониторинг-работы)
- [Автоматические тесты](#автоматические-тесты)

---

## Запуск сервера

```bash
# Сборка и запуск
./build-and-run.sh

# Или простой запуск (если уже собран)
./run-weather-server.sh
```

Сервер запустится на `http://localhost:8080`

---

## Тестирование endpoints

### 1. Health Check - Проверка здоровья сервера

```bash
curl http://localhost:8080/health
```

**Ожидаемый ответ:**
```
OK
```

### 2. Главная страница

```bash
curl http://localhost:8080/
```

**Ожидаемый ответ:**
```
Weather AI Server is running!
```

### 3. Получение последних 10 записей

```bash
curl http://localhost:8080/weather/latest
```

**Ожидаемый ответ (JSON):**
```json
[
  {
    "id": 5,
    "timestamp": "2026-02-03T10:20:00Z",
    "weatherResponse": "В Москве сейчас облачно с прояснениями. Температура воздуха составляет около -5°C. Ожидаются небольшие снегопады. Ветер северо-западный, скорость около 5 м/с.",
    "modelUsed": "openai/gpt-4o-mini"
  },
  {
    "id": 4,
    "timestamp": "2026-02-03T10:19:00Z",
    "weatherResponse": "Сейчас в Москве пасмурно, температура -6°C. Без осадков, но возможны снежные заряды ближе к вечеру.",
    "modelUsed": "openai/gpt-4o-mini"
  }
]
```

### 4. Получение всех записей

```bash
curl http://localhost:8080/weather/all
```

**Ожидаемый ответ:** Массив всех записей из БД (аналогично `/weather/latest`)

### 5. Ручной запрос погоды

```bash
curl http://localhost:8080/weather/check-now
```

**Ожидаемый ответ:**
```
Weather check initiated
```

Это инициирует немедленный запрос погоды (не дожидаясь следующей минуты)

---

## Примеры использования

### С форматированием JSON (jq)

```bash
# Установка jq (если не установлен)
# macOS:
brew install jq

# Ubuntu/Debian:
sudo apt install jq -y

# Получить последние записи с красивым форматированием
curl -s http://localhost:8080/weather/latest | jq '.'

# Получить только текст последнего ответа
curl -s http://localhost:8080/weather/latest | jq '.[0].weatherResponse'

# Получить только ID и время
curl -s http://localhost:8080/weather/latest | jq '.[] | {id, timestamp}'

# Подсчитать количество записей
curl -s http://localhost:8080/weather/all | jq 'length'

# Получить все уникальные модели
curl -s http://localhost:8080/weather/all | jq '[.[].modelUsed] | unique'
```

### С httpie (красивый HTTP клиент)

```bash
# Установка httpie
# macOS:
brew install httpie

# Ubuntu/Debian:
sudo apt install httpie

# Использование
http localhost:8080/health
http localhost:8080/weather/latest
http localhost:8080/weather/check-now
```

### Из Python

```python
import requests

# Health check
response = requests.get('http://localhost:8080/health')
print(response.text)  # OK

# Получить последние записи
response = requests.get('http://localhost:8080/weather/latest')
data = response.json()

for record in data:
    print(f"ID: {record['id']}")
    print(f"Время: {record['timestamp']}")
    print(f"Погода: {record['weatherResponse']}")
    print(f"Модель: {record['modelUsed']}")
    print("-" * 50)

# Инициировать проверку погоды
response = requests.get('http://localhost:8080/weather/check-now')
print(response.text)  # Weather check initiated
```

### Из JavaScript (Node.js)

```javascript
// Получить последние записи
fetch('http://localhost:8080/weather/latest')
  .then(response => response.json())
  .then(data => {
    console.log('Последние записи о погоде:');
    data.forEach(record => {
      console.log(`\nID: ${record.id}`);
      console.log(`Время: ${record.timestamp}`);
      console.log(`Погода: ${record.weatherResponse}`);
      console.log(`Модель: ${record.modelUsed}`);
    });
  })
  .catch(error => console.error('Ошибка:', error));

// Инициировать проверку
fetch('http://localhost:8080/weather/check-now')
  .then(response => response.text())
  .then(text => console.log(text));
```

### Из Kotlin

```kotlin
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class WeatherRecord(
    val id: Int,
    val timestamp: String,
    val weatherResponse: String,
    val modelUsed: String
)

suspend fun main() {
    val client = HttpClient()
    
    // Health check
    val health: String = client.get("http://localhost:8080/health").body()
    println("Health: $health")
    
    // Получить последние записи
    val records: List<WeatherRecord> = 
        client.get("http://localhost:8080/weather/latest").body()
    
    records.forEach { record ->
        println("ID: ${record.id}")
        println("Время: ${record.timestamp}")
        println("Погода: ${record.weatherResponse}")
        println("Модель: ${record.modelUsed}")
        println("-".repeat(50))
    }
    
    client.close()
}
```

---

## Мониторинг работы

### Просмотр логов в реальном времени

```bash
# Основные логи
tail -f logs/weather-agent.log

# С grep для фильтрации
tail -f logs/weather-agent.log | grep "WeatherAgent"

# Только ошибки
tail -f logs/weather-agent.log | grep "ERROR"

# Только успешные сохранения в БД
tail -f logs/weather-agent.log | grep "Запись сохранена"
```

### Скрипт для мониторинга

```bash
# Создайте файл monitor.sh
cat > monitor.sh << 'EOF'
#!/bin/bash

echo "╔═══════════════════════════════════════════════════╗"
echo "║   Weather AI Server - Мониторинг                 ║"
echo "╚═══════════════════════════════════════════════════╝"

while true; do
    clear
    echo "Время: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    
    # Health check
    echo "🏥 Health Check:"
    curl -s http://localhost:8080/health
    echo ""
    echo ""
    
    # Количество записей
    echo "📊 Количество записей в БД:"
    RECORDS=$(curl -s http://localhost:8080/weather/all | jq 'length')
    echo "   Всего записей: $RECORDS"
    echo ""
    
    # Последняя запись
    echo "🌤️  Последняя запись:"
    LATEST=$(curl -s http://localhost:8080/weather/latest | jq '.[0]')
    echo "$LATEST" | jq '.'
    echo ""
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Обновление через 30 секунд... (Ctrl+C для выхода)"
    
    sleep 30
done
EOF

chmod +x monitor.sh
./monitor.sh
```

### Проверка ресурсов

```bash
# Использование памяти процессом Java
ps aux | grep java

# Размер базы данных
du -sh data/

# Размер логов
du -sh logs/

# Проверка порта
netstat -an | grep 8080
# или
lsof -i :8080
```

---

## Автоматические тесты

### Bash скрипт для тестирования

```bash
# Создайте файл test.sh
cat > test.sh << 'EOF'
#!/bin/bash

set -e

echo "╔═══════════════════════════════════════════════════╗"
echo "║   Weather AI Server - Автоматические тесты       ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

BASE_URL="http://localhost:8080"
PASSED=0
FAILED=0

test_endpoint() {
    local name=$1
    local endpoint=$2
    local expected=$3
    
    echo -n "Тест: $name... "
    
    response=$(curl -s "$BASE_URL$endpoint")
    
    if [[ $response == *"$expected"* ]]; then
        echo "✅ PASSED"
        ((PASSED++))
    else
        echo "❌ FAILED"
        echo "   Ожидалось: $expected"
        echo "   Получено: $response"
        ((FAILED++))
    fi
}

test_json_endpoint() {
    local name=$1
    local endpoint=$2
    
    echo -n "Тест: $name... "
    
    response=$(curl -s "$BASE_URL$endpoint")
    
    if echo "$response" | jq . > /dev/null 2>&1; then
        echo "✅ PASSED (валидный JSON)"
        ((PASSED++))
    else
        echo "❌ FAILED (невалидный JSON)"
        echo "   Получено: $response"
        ((FAILED++))
    fi
}

# Тесты
test_endpoint "Health Check" "/health" "OK"
test_endpoint "Home Page" "/" "Weather AI Server is running"
test_json_endpoint "Latest Weather" "/weather/latest"
test_json_endpoint "All Weather" "/weather/all"
test_endpoint "Check Now" "/weather/check-now" "Weather check initiated"

# Результаты
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Результаты тестирования:"
echo "   ✅ Пройдено: $PASSED"
echo "   ❌ Провалено: $FAILED"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $FAILED -eq 0 ]; then
    echo "🎉 Все тесты пройдены успешно!"
    exit 0
else
    echo "⚠️  Некоторые тесты провалены"
    exit 1
fi
EOF

chmod +x test.sh
./test.sh
```

### Python тесты (pytest)

```python
# Создайте файл test_api.py
import requests
import pytest
import time

BASE_URL = "http://localhost:8080"

def test_health_check():
    """Проверка endpoint health"""
    response = requests.get(f"{BASE_URL}/health")
    assert response.status_code == 200
    assert response.text == "OK"

def test_home_page():
    """Проверка главной страницы"""
    response = requests.get(f"{BASE_URL}/")
    assert response.status_code == 200
    assert "Weather AI Server is running" in response.text

def test_latest_weather():
    """Проверка получения последних записей"""
    response = requests.get(f"{BASE_URL}/weather/latest")
    assert response.status_code == 200
    
    data = response.json()
    assert isinstance(data, list)
    
    if len(data) > 0:
        record = data[0]
        assert "id" in record
        assert "timestamp" in record
        assert "weatherResponse" in record
        assert "modelUsed" in record

def test_all_weather():
    """Проверка получения всех записей"""
    response = requests.get(f"{BASE_URL}/weather/all")
    assert response.status_code == 200
    
    data = response.json()
    assert isinstance(data, list)

def test_check_now():
    """Проверка ручного запроса погоды"""
    response = requests.get(f"{BASE_URL}/weather/check-now")
    assert response.status_code == 200
    assert "Weather check initiated" in response.text
    
    # Подождать немного
    time.sleep(3)
    
    # Проверить, что появилась новая запись
    response = requests.get(f"{BASE_URL}/weather/latest")
    data = response.json()
    assert len(data) > 0

def test_automatic_updates():
    """Проверка автоматического обновления каждую минуту"""
    # Получить текущее количество записей
    response = requests.get(f"{BASE_URL}/weather/all")
    initial_count = len(response.json())
    
    # Подождать 65 секунд (минута + запас)
    print("Ожидание автоматического обновления (65 сек)...")
    time.sleep(65)
    
    # Проверить, что появилась новая запись
    response = requests.get(f"{BASE_URL}/weather/all")
    new_count = len(response.json())
    
    assert new_count > initial_count, "Автоматическое обновление не сработало"

if __name__ == "__main__":
    pytest.main([__file__, "-v"])
```

Запуск:

```bash
# Установка pytest
pip install pytest requests

# Запуск тестов
pytest test_api.py -v
```

---

## Нагрузочное тестирование

### С помощью Apache Bench (ab)

```bash
# Установка
# macOS:
# ab обычно предустановлен

# Ubuntu/Debian:
sudo apt install apache2-utils

# Тестирование
ab -n 1000 -c 10 http://localhost:8080/health

# -n 1000: 1000 запросов
# -c 10: 10 одновременных соединений
```

### С помощью wrk

```bash
# Установка
# macOS:
brew install wrk

# Ubuntu/Debian:
sudo apt install wrk

# Тестирование
wrk -t4 -c100 -d30s http://localhost:8080/health

# -t4: 4 потока
# -c100: 100 соединений
# -d30s: 30 секунд
```

---

## Полезные команды для отладки

```bash
# Проверка, запущен ли сервер
curl -f http://localhost:8080/health && echo "Сервер работает" || echo "Сервер не отвечает"

# Получить заголовки ответа
curl -I http://localhost:8080/health

# Подробный вывод
curl -v http://localhost:8080/weather/latest

# Время ответа
curl -w "\nВремя ответа: %{time_total}s\n" -o /dev/null -s http://localhost:8080/health

# Сохранить ответ в файл
curl http://localhost:8080/weather/all > weather_data.json
```

---

**Удачного тестирования! 🧪**
