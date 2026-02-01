# 🏗️ Архитектура MCP Client

## Обзор

MCP Client для Kotlin построен на основе следующих принципов:

- **Модульность**: Четкое разделение ответственности
- **Асинхронность**: Использование Kotlin корутин
- **Типобезопасность**: Строгая типизация с kotlinx.serialization
- **Расширяемость**: Легко добавлять новые возможности

## Структура проекта

```
day11/
├── src/main/kotlin/
│   ├── Main.kt                           # Точка входа основного приложения
│   └── org/example/
│       ├── examples/                     # Примеры использования
│       │   ├── BasicExample.kt
│       │   ├── ToolCallExample.kt
│       │   ├── ConfigFileExample.kt
│       │   ├── BatchOperationsExample.kt
│       │   └── InteractiveCli.kt
│       └── mcp/                          # Основная библиотека
│           ├── McpClient.kt              # Главный клиент
│           ├── McpClientFactory.kt       # Фабрика для создания клиентов
│           ├── McpConfig.kt              # Конфигурация
│           ├── McpUtils.kt               # Утилиты
│           └── models/                   # Модели данных
│               ├── JsonRpc.kt            # JSON-RPC 2.0 модели
│               └── McpModels.kt          # MCP специфичные модели
├── src/main/resources/
│   ├── logback.xml                       # Конфигурация логирования
│   └── mcp-config.json                   # Пример конфигурации
├── src/test/kotlin/
│   └── org/example/mcp/
│       └── McpClientTest.kt              # Тесты
├── build.gradle.kts                      # Конфигурация сборки
├── README.md                             # Основная документация
├── EXAMPLES.md                           # Примеры использования
├── QUICKSTART.md                         # Быстрый старт
├── ARCHITECTURE.md                       # Этот файл
└── run-*.sh                              # Скрипты запуска
```

## Основные компоненты

### 1. McpClient

**Назначение:** Главный класс для взаимодействия с MCP сервером.

**Ответственность:**
- Управление соединением с сервером
- Отправка JSON-RPC запросов
- Обработка ответов и ошибок
- Управление состоянием (инициализация)

**Основные методы:**
```kotlin
suspend fun initialize(): InitializeResult
suspend fun listTools(): ListToolsResult
suspend fun callTool(name: String, arguments: JsonObject?): CallToolResult
suspend fun listResources(): ListResourcesResult
suspend fun readResource(uri: String): ReadResourceResult
suspend fun listPrompts(): ListPromptsResult
fun close()
```

**Особенности:**
- Thread-safe инициализация с использованием Mutex
- Автоматическая инициализация при первом вызове
- Кастомизируемые HTTP заголовки
- Подробное логирование через Ktor

### 2. McpClientFactory

**Назначение:** Упрощение создания клиентов из различных источников конфигурации.

**Методы:**
```kotlin
fun fromConfigFile(configPath: String, serverName: String): McpClient
fun fromConfigJson(configJson: String, serverName: String): McpClient
fun fromConfig(config: McpConfig): McpClient
fun listServers(configPath: String): List<String>
```

### 3. McpConfig

**Назначение:** Хранение конфигурации подключения.

**Структура:**
```kotlin
data class McpConfig(
    val url: String,
    val headers: Map<String, String> = emptyMap()
)
```

### 4. McpUtils

**Назначение:** Вспомогательные функции для работы с MCP.

**Функции:**
- `findTool()` - поиск инструмента по имени
- `toolExists()` - проверка существования инструмента
- `callToolsInParallel()` - параллельный вызов инструментов
- `prettyPrintTool()` - форматированный вывод инструмента
- `extractText()` - извлечение текста из результата

## Модели данных

### JSON-RPC 2.0 (JsonRpc.kt)

Реализация стандарта JSON-RPC 2.0:

```kotlin
JsonRpcRequest      # Запрос
JsonRpcResponse     # Ответ
JsonRpcError        # Ошибка
JsonRpcNotification # Уведомление (без id)
```

### MCP Models (McpModels.kt)

Специфичные для MCP модели:

```kotlin
# Инициализация
InitializeParams
InitializeResult
ClientInfo / ServerInfo
ClientCapabilities / ServerCapabilities

# Ресурсы
Resource
ListResourcesResult
ReadResourceParams
ReadResourceResult

# Инструменты
Tool
ListToolsResult
CallToolParams
CallToolResult

# Промпты
Prompt
ListPromptsResult
```

## Поток выполнения

### 1. Инициализация клиента

```
User Code
    ↓
McpClient.initialize()
    ↓
Send JSON-RPC Request "initialize"
    ↓
Receive InitializeResult
    ↓
Store serverInfo & capabilities
    ↓
Mark as initialized
```

### 2. Вызов инструмента

```
User Code
    ↓
McpClient.callTool(name, args)
    ↓
Check if initialized (auto-init if needed)
    ↓
Build JsonRpcRequest
    ↓
Send HTTP POST to server
    ↓
Parse JsonRpcResponse
    ↓
Handle errors or return result
    ↓
Return CallToolResult
```

### 3. Batch операции

```
User Code
    ↓
McpUtils.callToolsInParallel(calls)
    ↓
Create coroutine for each call
    ↓
Launch all coroutines in parallel
    ↓
Await all results
    ↓
Return List<Result<CallToolResult>>
```

## Обработка ошибок

### Уровни обработки

1. **HTTP уровень** - Ktor обрабатывает HTTP ошибки
2. **JSON-RPC уровень** - Проверка `error` в ответе
3. **Application уровень** - `McpException` с деталями ошибки

### McpException

```kotlin
class McpException(
    message: String,
    val error: JsonRpcError? = null
) : Exception(message)
```

**Содержит:**
- Текстовое описание ошибки
- Код ошибки JSON-RPC
- Дополнительные данные (если есть)

## Асинхронность

### Использование корутин

Все сетевые операции - suspend функции:

```kotlin
suspend fun initialize(): InitializeResult
suspend fun listTools(): ListToolsResult
suspend fun callTool(...): CallToolResult
```

**Преимущества:**
- Неблокирующий I/O
- Простой синтаксис
- Легкая композиция операций
- Отмена операций
- Структурированная конкурентность

### Параллельное выполнение

```kotlin
coroutineScope {
    val deferred1 = async { client.callTool("tool1", args1) }
    val deferred2 = async { client.callTool("tool2", args2) }
    val result1 = deferred1.await()
    val result2 = deferred2.await()
}
```

## HTTP клиент (Ktor)

### Конфигурация

```kotlin
HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
}
```

### Особенности

- **Engine:** CIO (Coroutine I/O)
- **Content Negotiation:** Автоматическая сериализация/десериализация JSON
- **Logging:** Логирование запросов/ответов
- **Custom Headers:** Поддержка кастомных заголовков (API ключи)

## Сериализация (kotlinx.serialization)

### Аннотации

```kotlin
@Serializable
data class Tool(
    val name: String,
    val description: String? = null,
    val inputSchema: JsonObject
)
```

### JSON конфигурация

```kotlin
Json {
    prettyPrint = true       # Красивый вывод
    isLenient = true         # Допустимые отклонения от стандарта
    ignoreUnknownKeys = true # Игнорировать неизвестные поля
}
```

## Безопасность

### API ключи

- Хранятся в переменных окружения
- Передаются через HTTP заголовки
- Не хардкодятся в коде
- Поддержка `.gitignore` для локальных конфигов

### HTTPS

- Все соединения через HTTPS
- Валидация сертификатов
- Безопасная передача данных

## Расширяемость

### Добавление новых методов MCP

1. Добавить модели в `McpModels.kt`:
```kotlin
@Serializable
data class NewMethodParams(...)

@Serializable
data class NewMethodResult(...)
```

2. Добавить метод в `McpClient`:
```kotlin
suspend fun newMethod(params: NewMethodParams): NewMethodResult {
    ensureInitialized()
    return sendRequest(
        method = "new/method",
        params = json.encodeToJsonElement(params).jsonObject
    )
}
```

### Добавление новых транспортов

Текущая реализация использует HTTP POST. Можно добавить:

- WebSocket транспорт
- Server-Sent Events (SSE)
- stdio транспорт

## Производительность

### Оптимизации

1. **Connection Pooling** - Ktor переиспользует соединения
2. **Parallel Requests** - McpUtils.callToolsInParallel()
3. **Lazy Initialization** - Инициализация при первом запросе
4. **Efficient JSON** - kotlinx.serialization (быстрее Gson/Jackson)

### Метрики

- Время инициализации: ~500-1000ms
- Время вызова инструмента: ~200-500ms
- Batch операции: N запросов за ~200-500ms (параллельно)

## Тестирование

### Типы тестов

1. **Unit тесты** - Тестирование отдельных компонентов
2. **Integration тесты** - Тестирование с реальным сервером
3. **Example тесты** - Исполняемые примеры

### Запуск

```bash
./gradlew test
```

## Логирование

### Уровни

- `INFO` - Основные операции
- `DEBUG` - HTTP запросы/ответы
- `ERROR` - Ошибки

### Конфигурация

`logback.xml` - настройка формата и уровня логирования

## Зависимости

### Основные

- `kotlinx-coroutines-core` - Корутины
- `ktor-client-*` - HTTP клиент
- `kotlinx-serialization-json` - JSON сериализация
- `logback-classic` - Логирование

### Граф зависимостей

```
McpClient
    ↓
Ktor Client → kotlinx-serialization
    ↓                ↓
kotlinx-coroutines  JsonRpc Models
                        ↓
                    MCP Models
```

## Будущие улучшения

1. **Кеширование** - Кеш результатов запросов
2. **Retry механизм** - Автоматические повторы при ошибках
3. **Метрики** - Сбор статистики использования
4. **WebSocket** - Поддержка WebSocket транспорта
5. **Streaming** - Поддержка потоковых ответов
6. **Authentication** - Расширенные методы аутентификации

## Лучшие практики

### При использовании клиента

1. Всегда закрывайте клиент: `client.close()`
2. Используйте try-finally блоки
3. Обрабатывайте McpException
4. Храните API ключи в переменных окружения
5. Переиспользуйте один клиент для множества запросов

### При разработке

1. Следуйте Kotlin coding conventions
2. Пишите suspend функции для I/O операций
3. Используйте data классы для моделей
4. Документируйте публичные API
5. Покрывайте код тестами
