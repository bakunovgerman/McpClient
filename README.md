# 🚀 MCP Client для Kotlin

[![](https://jitpack.io/v/germanbakunov/McpClient.svg)](https://jitpack.io/#germanbakunov/McpClient)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Полнофункциональный клиент для подключения к удаленным MCP (Model Context Protocol) серверам.

## ✨ Возможности

- ✅ Подключение к удаленным MCP серверам через HTTP/HTTPS
- ✅ Полная поддержка JSON-RPC 2.0 протокола
- ✅ Работа с инструментами (Tools) - вызов и управление
- ✅ Работа с ресурсами (Resources) - список и чтение
- ✅ Работа с промптами (Prompts)
- ✅ Настройка пользовательских заголовков (API ключи, токены)
- ✅ Асинхронная работа с корутинами Kotlin
- ✅ Batch операции - параллельное выполнение запросов
- ✅ Удобные утилиты для работы с MCP
- ✅ Интерактивный CLI
- ✅ Подробное логирование
- ✅ Обработка ошибок

## Требования

- Java 24+
- Kotlin 2.2.21+

## Установка

### Вариант 1: Использование как Gradle зависимости (Рекомендуется)

Добавьте JitPack репозиторий в ваш проект:

**settings.gradle.kts (Kotlin DSL):**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**settings.gradle (Groovy):**
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Добавьте зависимость в ваш модуль:

**build.gradle.kts (Kotlin DSL):**
```kotlin
dependencies {
    implementation("com.github.germanbakunov:McpClient:1.0.0")
}
```

**build.gradle (Groovy):**
```groovy
dependencies {
    implementation 'com.github.germanbakunov:McpClient:1.0.0'
}
```

> 📘 Подробнее о публикации и использовании смотрите в [PUBLISHING.md](PUBLISHING.md)

### Вариант 2: Клонирование репозитория

1. Клонируйте репозиторий
2. Установите зависимости:

```bash
./gradlew build
```

## Конфигурация

### Способ 1: Файл local.properties (рекомендуется)

Создайте файл `local.properties` в корне проекта:

```properties
# Local configuration file
CONTEXT7_API_KEY=your_actual_api_key_here
```

Файл `local.properties` уже добавлен в `.gitignore`, поэтому ваши API ключи не попадут в git.

Все примеры автоматически загружают токен из этого файла:

```kotlin
import org.example.mcp.McpUtils

val apiKey = McpUtils.requireProperty("local.properties", "CONTEXT7_API_KEY")
```

### Способ 2: Конфигурационный файл JSON

Пример конфигурации для подключения к Context7:

```json
{
  "mcpServers": {
    "context7": {
      "url": "https://mcp.context7.com/mcp",
      "headers": {
        "CONTEXT7_API_KEY": "YOUR_API_KEY"
      }
    }
  }
}
```

## Использование

### Базовый пример

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.McpUtils

fun main() = runBlocking {
    // Загружаем API ключ из local.properties
    val apiKey = McpUtils.requireProperty("local.properties", "CONTEXT7_API_KEY")
    
    // Создаем конфигурацию
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf(
            "CONTEXT7_API_KEY" to apiKey
        )
    )

    // Создаем клиент
    val client = McpClient(config)

    try {
        // Инициализация соединения
        client.initialize()

        // Получение списка инструментов
        val tools = client.listTools()
        tools.tools.forEach { tool ->
            println("Tool: ${tool.name}")
            println("Description: ${tool.description}")
        }

        // Вызов инструмента
        val arguments = buildJsonObject {
            put("libraryName", "react")
            put("query", "How to use React hooks?")
        }
        val result = client.callTool("resolve-library-id", arguments)
        
    } finally {
        client.close()
    }
}
```

### Работа с ресурсами

```kotlin
// Получение списка ресурсов
val resources = client.listResources()

// Чтение конкретного ресурса
val content = client.readResource("resource://example")
```

### Работа с промптами

```kotlin
// Получение списка промптов
val prompts = client.listPrompts()
```

## 🎯 Быстрый старт

### 1. Установка переменной окружения

```bash
export CONTEXT7_API_KEY="your_api_key_here"
```

### 2. Запуск базового примера

```bash
./gradlew run
# или
./run-basic.sh
```

### 3. Другие примеры

```bash
# Вызов инструментов
./run-tool-call.sh

# Batch операции
./run-batch.sh

# Интерактивный CLI
./run-interactive.sh
```

## Структура проекта

```
src/
├── main/
│   └── kotlin/
│       ├── Main.kt                     # Пример использования
│       └── org/example/mcp/
│           ├── McpClient.kt           # Основной клиент
│           ├── McpConfig.kt           # Конфигурация
│           └── models/
│               ├── JsonRpc.kt         # JSON-RPC модели
│               └── McpModels.kt       # MCP модели данных
```

## API

### McpClient

#### Методы

- `suspend fun initialize(): InitializeResult` - Инициализация соединения
- `suspend fun listTools(): ListToolsResult` - Список доступных инструментов
- `suspend fun callTool(name: String, arguments: JsonObject?): CallToolResult` - Вызов инструмента
- `suspend fun listResources(): ListResourcesResult` - Список доступных ресурсов
- `suspend fun readResource(uri: String): ReadResourceResult` - Чтение ресурса
- `suspend fun listPrompts(): ListPromptsResult` - Список доступных промптов
- `fun close()` - Закрытие соединения

## Обработка ошибок

```kotlin
try {
    val result = client.callTool("some-tool", arguments)
} catch (e: McpException) {
    println("MCP Error: ${e.message}")
    println("Error code: ${e.error?.code}")
}
```

## Логирование

Клиент использует Ktor Logging для вывода HTTP запросов/ответов. Уровень логирования можно настроить через logback.

## Лицензия

MIT

## 📚 Примеры

Подробные примеры использования смотрите в [EXAMPLES.md](EXAMPLES.md)

Доступные примеры кода:
- `src/main/kotlin/org/example/examples/BasicExample.kt` - Базовый пример
- `src/main/kotlin/org/example/examples/ToolCallExample.kt` - Вызов инструментов
- `src/main/kotlin/org/example/examples/ConfigFileExample.kt` - Работа с конфигурацией
- `src/main/kotlin/org/example/examples/BatchOperationsExample.kt` - Batch операции
- `src/main/kotlin/org/example/examples/InteractiveCli.kt` - Интерактивный CLI

## 🧪 Тестирование

```bash
# Сборка проекта
./gradlew build

# Запуск тестов
./gradlew test

# Очистка и пересборка
./gradlew clean build
```

## 🔧 Расширенная настройка

### Кастомные заголовки

```kotlin
val config = McpConfig(
    url = "https://your-server.com/mcp",
    headers = mapOf(
        "Authorization" to "Bearer YOUR_TOKEN",
        "X-Custom-Header" to "value"
    )
)
```

### Кастомное имя клиента

```kotlin
val client = McpClient(
    config = config,
    clientName = "MyApplication",
    clientVersion = "1.0.0"
)
```

## 🤝 Вклад в проект

Вклад приветствуется! Пожалуйста:
1. Форкните репозиторий
2. Создайте ветку для фичи (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Запушьте в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📝 TODO

- [ ] Поддержка WebSocket транспорта
- [ ] Поддержка Server-Sent Events (SSE)
- [ ] Кеширование запросов
- [ ] Retry механизм с экспоненциальной задержкой
- [ ] Метрики и мониторинг
- [ ] Более подробная документация API

## 🐛 Известные проблемы

Пока нет известных проблем. Если вы нашли баг, пожалуйста, создайте issue.

## 📄 Лицензия

MIT

## 🔗 Полезные ссылки

- [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/)
- [Context7 MCP Server](https://context7.com)
- [Context7 Documentation](https://context7.com/docs)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Ktor Client](https://ktor.io/docs/client.html)

## 👤 Автор

Создано для работы с MCP серверами на Kotlin.

## 🙏 Благодарности

- Команде Kotlin за отличный язык
- Команде Ktor за мощный HTTP клиент
- Команде Context7 за MCP сервер
