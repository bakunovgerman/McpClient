# Примеры использования MCP Client как зависимости

## Быстрый старт

### 1. Добавьте зависимость

**settings.gradle.kts:**
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**build.gradle.kts:**
```kotlin
dependencies {
    implementation("com.github.germanbakunov:McpClient:1.0.0")
}
```

### 2. Используйте в вашем коде

## Пример 1: Простой запрос к MCP серверу

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    // Создать конфигурацию
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf(
            "CONTEXT7_API_KEY" to "your_api_key_here"
        )
    )
    
    // Создать клиент
    val client = McpClient(config)
    
    try {
        // Инициализировать соединение
        client.initialize()
        
        // Получить список инструментов
        val tools = client.listTools()
        println("Available tools:")
        tools.tools.forEach { tool ->
            println("- ${tool.name}: ${tool.description}")
        }
    } finally {
        client.close()
    }
}
```

## Пример 2: Вызов инструмента

```kotlin
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf("CONTEXT7_API_KEY" to System.getenv("CONTEXT7_API_KEY"))
    )
    
    val client = McpClient(config)
    
    try {
        client.initialize()
        
        // Подготовить аргументы
        val arguments = buildJsonObject {
            put("libraryName", "react")
            put("query", "How to use React hooks?")
        }
        
        // Вызвать инструмент
        val result = client.callTool("resolve-library-id", arguments)
        
        // Обработать результат
        result.content.forEach { content ->
            when {
                content.type == "text" -> {
                    println("Result: ${content.text}")
                }
            }
        }
    } finally {
        client.close()
    }
}
```

## Пример 3: Использование в Android приложении

### ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

class McpViewModel : ViewModel() {
    private val _result = MutableStateFlow<String>("")
    val result: StateFlow<String> = _result
    
    private lateinit var client: McpClient
    
    fun initialize(apiKey: String) {
        viewModelScope.launch {
            try {
                val config = McpConfig(
                    url = "https://mcp.context7.com/mcp",
                    headers = mapOf("CONTEXT7_API_KEY" to apiKey)
                )
                
                client = McpClient(config)
                client.initialize()
                
                _result.value = "Connected successfully"
            } catch (e: Exception) {
                _result.value = "Error: ${e.message}"
            }
        }
    }
    
    fun getTools() {
        viewModelScope.launch {
            try {
                val tools = client.listTools()
                _result.value = tools.tools.joinToString("\n") { 
                    "${it.name}: ${it.description}" 
                }
            } catch (e: Exception) {
                _result.value = "Error: ${e.message}"
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (::client.isInitialized) {
            client.close()
        }
    }
}
```

### Composable UI

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun McpScreen(viewModel: McpViewModel = viewModel()) {
    val result by viewModel.result.collectAsState()
    var apiKey by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { viewModel.initialize(apiKey) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { viewModel.getTools() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Tools")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = result,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

## Пример 4: Работа с ресурсами

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf("CONTEXT7_API_KEY" to System.getenv("CONTEXT7_API_KEY"))
    )
    
    val client = McpClient(config)
    
    try {
        client.initialize()
        
        // Получить список ресурсов
        val resources = client.listResources()
        println("Available resources:")
        resources.resources.forEach { resource ->
            println("- ${resource.name}: ${resource.uri}")
            println("  ${resource.description}")
        }
        
        // Прочитать конкретный ресурс (если доступен)
        if (resources.resources.isNotEmpty()) {
            val firstResource = resources.resources.first()
            val content = client.readResource(firstResource.uri)
            
            content.contents.forEach { item ->
                when (item.mimeType) {
                    "text/plain" -> println("Text content: ${item.text}")
                    "application/json" -> println("JSON content: ${item.text}")
                }
            }
        }
    } finally {
        client.close()
    }
}
```

## Пример 5: Batch операции

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf("CONTEXT7_API_KEY" to System.getenv("CONTEXT7_API_KEY"))
    )
    
    val client = McpClient(config)
    
    try {
        client.initialize()
        
        // Выполнить несколько запросов параллельно
        val libraries = listOf("react", "vue", "angular")
        
        val results = libraries.map { library ->
            async {
                val arguments = buildJsonObject {
                    put("libraryName", library)
                    put("query", "latest documentation")
                }
                
                library to client.callTool("resolve-library-id", arguments)
            }
        }.awaitAll()
        
        // Обработать результаты
        results.forEach { (library, result) ->
            println("Results for $library:")
            result.content.forEach { content ->
                if (content.type == "text") {
                    println("  ${content.text}")
                }
            }
        }
    } finally {
        client.close()
    }
}
```

## Пример 6: Обработка ошибок

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.models.McpException

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf("CONTEXT7_API_KEY" to "invalid_key")
    )
    
    val client = McpClient(config)
    
    try {
        client.initialize()
        
        val tools = client.listTools()
        println("Tools: ${tools.tools}")
    } catch (e: McpException) {
        // Специфичная ошибка MCP
        println("MCP Error:")
        println("  Code: ${e.error?.code}")
        println("  Message: ${e.error?.message}")
        println("  Data: ${e.error?.data}")
    } catch (e: Exception) {
        // Общая ошибка
        println("General error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
    }
}
```

## Пример 7: Кастомные заголовки и таймауты

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf(
            "CONTEXT7_API_KEY" to System.getenv("CONTEXT7_API_KEY"),
            "User-Agent" to "MyApp/1.0.0",
            "X-Custom-Header" to "custom_value"
        )
    )
    
    val client = McpClient(
        config = config,
        clientName = "MyApplication",
        clientVersion = "1.0.0"
    )
    
    try {
        client.initialize()
        
        // Использовать клиент
        val tools = client.listTools()
        println("Tools: ${tools.tools.size}")
    } finally {
        client.close()
    }
}
```

## Пример 8: Spring Boot интеграция

```kotlin
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value

@Configuration
class McpConfiguration {
    
    @Value("\${mcp.context7.url}")
    private lateinit var mcpUrl: String
    
    @Value("\${mcp.context7.apiKey}")
    private lateinit var apiKey: String
    
    @Bean
    fun mcpClient(): McpClient {
        val config = McpConfig(
            url = mcpUrl,
            headers = mapOf("CONTEXT7_API_KEY" to apiKey)
        )
        
        return McpClient(config)
    }
}

// Сервис для использования MCP
import org.springframework.stereotype.Service
import kotlinx.coroutines.runBlocking

@Service
class McpService(private val mcpClient: McpClient) {
    
    fun initialize() = runBlocking {
        mcpClient.initialize()
    }
    
    fun getTools() = runBlocking {
        mcpClient.listTools()
    }
    
    fun callTool(name: String, arguments: JsonObject?) = runBlocking {
        mcpClient.callTool(name, arguments)
    }
}
```

**application.properties:**
```properties
mcp.context7.url=https://mcp.context7.com/mcp
mcp.context7.apiKey=${CONTEXT7_API_KEY}
```

## Дополнительные ресурсы

- [ANDROID_USAGE.md](ANDROID_USAGE.md) - Подробное использование в Android
- [EXAMPLES.md](EXAMPLES.md) - Расширенные примеры из репозитория
- [PUBLISHING.md](PUBLISHING.md) - Как публиковать свои изменения

## Поддержка

Если у вас возникли вопросы или проблемы:
1. Проверьте [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
2. Создайте issue на GitHub
3. Посмотрите примеры в директории `src/main/kotlin/org/example/examples/`
