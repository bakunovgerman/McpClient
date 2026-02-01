# MCP Client - Примеры использования

## Содержание

1. [Базовый пример](#базовый-пример)
2. [Вызов инструментов](#вызов-инструментов)
3. [Работа с конфигурацией](#работа-с-конфигурацией)
4. [Batch операции](#batch-операции)
5. [Интерактивный CLI](#интерактивный-cli)

## Базовый пример

Простейший пример подключения к MCP серверу:

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf(
            "CONTEXT7_API_KEY" to "YOUR_API_KEY"
        )
    )

    val client = McpClient(config)
    
    try {
        // Инициализация
        val initResult = client.initialize()
        println("Connected to: ${initResult.serverInfo.name}")
        
        // Список инструментов
        val tools = client.listTools()
        tools.tools.forEach { tool ->
            println("Tool: ${tool.name}")
        }
    } finally {
        client.close()
    }
}
```

**Запуск:**
```bash
export CONTEXT7_API_KEY="your_key"
./run-basic.sh
```

## Вызов инструментов

### Resolve Library ID

```kotlin
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

val arguments = buildJsonObject {
    put("libraryName", "react")
    put("query", "How to use hooks?")
}

val result = client.callTool("resolve-library-id", arguments)
result.content.forEach { content ->
    println(content.text)
}
```

### Query Documentation

```kotlin
val arguments = buildJsonObject {
    put("libraryId", "/facebook/react")
    put("query", "How to use useState hook?")
}

val result = client.callTool("query-docs", arguments)
```

**Запуск:**
```bash
export CONTEXT7_API_KEY="your_key"
./run-tool-call.sh
```

## Работа с конфигурацией

### Конфигурационный файл

Создайте файл `mcp-config.json`:

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

### Использование

```kotlin
import org.example.mcp.McpClientFactory

fun main() = runBlocking {
    // Список серверов из конфига
    val servers = McpClientFactory.listServers("mcp-config.json")
    
    // Создание клиента
    val client = McpClientFactory.fromConfigFile(
        "mcp-config.json",
        "context7"
    )
    
    client.initialize()
    // ...
}
```

## Batch операции

Выполнение нескольких запросов параллельно:

```kotlin
import org.example.mcp.McpUtils

val libraries = listOf("react", "vue", "angular")

val calls = libraries.map { library ->
    library to buildJsonObject {
        put("libraryName", library)
        put("query", "Getting started with $library")
    }
}

// Параллельное выполнение
val results = McpUtils.callToolsInParallel(
    client,
    calls.map { (_, args) -> "resolve-library-id" to args }
)

// Обработка результатов
results.forEach { result ->
    result.fold(
        onSuccess = { println("Success!") },
        onFailure = { println("Error: ${it.message}") }
    )
}
```

**Запуск:**
```bash
export CONTEXT7_API_KEY="your_key"
./run-batch.sh
```

## Интерактивный CLI

Интерактивный режим для работы с MCP:

```bash
export CONTEXT7_API_KEY="your_key"
./run-interactive.sh
```

### Возможности CLI:

1. **List Tools** - Список всех доступных инструментов
2. **List Resources** - Список ресурсов
3. **List Prompts** - Список промптов
4. **Call Tool** - Вызов конкретного инструмента
5. **Show Server Info** - Информация о сервере

## Утилиты

### Проверка существования инструмента

```kotlin
val exists = McpUtils.toolExists(client, "resolve-library-id")
if (exists) {
    println("Tool is available")
}
```

### Поиск инструмента

```kotlin
val tool = McpUtils.findTool(client, "query-docs")
tool?.let {
    println(McpUtils.prettyPrintTool(it))
}
```

### Извлечение текста из результата

```kotlin
val result = client.callTool("some-tool", args)
val text = McpUtils.extractText(result)
println(text)
```

## Обработка ошибок

```kotlin
import org.example.mcp.McpException

try {
    val result = client.callTool("some-tool", args)
} catch (e: McpException) {
    println("MCP Error: ${e.message}")
    e.error?.let { error ->
        println("Code: ${error.code}")
        println("Message: ${error.message}")
        error.data?.let {
            println("Data: $it")
        }
    }
}
```

## Продвинутые примеры

### Кастомный клиент с настройками

```kotlin
val client = McpClient(
    config = config,
    clientName = "MyApp",
    clientVersion = "2.0.0"
)
```

### Работа с ресурсами

```kotlin
// Список ресурсов
val resources = client.listResources()

// Чтение ресурса
resources.resources.firstOrNull()?.let { resource ->
    val content = client.readResource(resource.uri)
    content.contents.forEach { resourceContent ->
        println("URI: ${resourceContent.uri}")
        println("Text: ${resourceContent.text}")
    }
}
```

### Последовательные вызовы

```kotlin
// Сначала resolve library
val resolveResult = client.callTool("resolve-library-id", buildJsonObject {
    put("libraryName", "react")
    put("query", "Getting started")
})

// Извлекаем library ID из результата
val libraryId = extractLibraryId(resolveResult)

// Затем query documentation
val docsResult = client.callTool("query-docs", buildJsonObject {
    put("libraryId", libraryId)
    put("query", "How to use useState?")
})
```

## Советы

1. **Переиспользование клиента**: Создавайте один клиент и используйте его для множества запросов
2. **Обработка ошибок**: Всегда оборачивайте вызовы в try-catch
3. **Закрытие ресурсов**: Используйте `try-finally` для вызова `client.close()`
4. **API ключи**: Храните ключи в переменных окружения, не в коде
5. **Batch операции**: Используйте параллельные вызовы для повышения производительности

## Дополнительная информация

- [README.md](README.md) - Основная документация
- [Context7 Documentation](https://context7.com/docs) - Документация Context7 MCP сервера
