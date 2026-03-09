# MCP Client for Kotlin/JVM

Клиентская библиотека для [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) на Kotlin/JVM. Поддерживает подключение к MCP-серверам по HTTP и через stdio (локальные процессы, в том числе npx).

## Возможности

- **HTTP-транспорт** — подключение к удалённым MCP-серверам по URL
- **Stdio-транспорт** — запуск локальных MCP-серверов как подпроцессов (npx, node, python и т.д.)
- **Подстановка переменных** — `${workspaceFolder}` в конфигурации
- **Единый API** — tools, resources, prompts через общий интерфейс

## Требования

- JDK 21+
- Gradle 8.x (или используйте wrapper)

## Быстрый старт

### Сборка

```bash
./gradlew build
```

### Запуск основного приложения

```bash
./gradlew run
```

### Примеры

**Файловая система (npx):**
```bash
./gradlew runFilesystemExample
```

**OpenRouter Poller:**
```bash
./gradlew runOpenRouterPoller
```

## Конфигурация

Конфигурация хранится в JSON-файле (по умолчанию `src/main/resources/mcp-config.json`).

### HTTP-сервер

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

### Stdio-сервер (npx)

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "${workspaceFolder}"
      ]
    }
  }
}
```

Поддерживается также формат Cursor с ключом `servers` вместо `mcpServers`.

## Использование в коде

### Из конфигурационного файла

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClientFactory

fun main() = runBlocking {
    val client = McpClientFactory.fromConfigFile(
        configPath = "mcp-config.json",
        serverName = "filesystem",
        workspaceFolder = "/path/to/workspace"  // по умолчанию — текущая директория
    )

    client.initialize()
    val tools = client.listTools()
    tools.tools.forEach { println(it.name) }

    val result = client.callTool("read_file", buildJsonObject {
        put("path", "build.gradle.kts")
    })

    client.close()
}
```

### Прямое создание клиента

**HTTP:**
```kotlin
val client = McpClient(McpConfig(
    url = "https://mcp.example.com/mcp",
    headers = mapOf("Authorization" to "Bearer TOKEN")
))
```

**Stdio (npx):**
```kotlin
val client = McpStdioClient(
    config = McpConfig(
        command = "npx",
        args = listOf("-y", "@modelcontextprotocol/server-filesystem", "\${workspaceFolder}")
    ),
    workspaceFolder = "/path/to/workspace"
)
```

## Структура проекта

```
src/main/kotlin/org/example/
├── mcp/                    # MCP-клиент
│   ├── McpClient.kt        # HTTP-транспорт
│   ├── McpStdioClient.kt   # Stdio-транспорт
│   ├── McpClientFactory.kt # Фабрика клиентов
│   ├── McpConfig.kt        # Конфигурация
│   └── models/             # Модели JSON-RPC
├── examples/               # Примеры использования
├── openrouter/             # Интеграция с OpenRouter
└── storage/                # Хранение ответов LLM
```

## Зависимости Maven

```xml
<dependency>
    <groupId>io.github.bakunovgerman</groupId>
    <artifactId>mcp-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Gradle (Kotlin DSL)

```kotlin
implementation("io.github.bakunovgerman:mcp-client:1.0.0")
```

## Лицензия

MIT License
