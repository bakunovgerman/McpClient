# Использование MCP Client в Android проекте

Это руководство показывает, как использовать MCP Client библиотеку в Android приложении.

## Установка

### 1. Добавьте JitPack репозиторий

**settings.gradle.kts (для современных Android проектов):**
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "YourApp"
include(":app")
```

### 2. Добавьте зависимость

**app/build.gradle.kts:**
```kotlin
dependencies {
    // MCP Client
    implementation("com.github.germanbakunov:McpClient:1.0.0")
    
    // Эти зависимости уже включены в MCP Client, но убедитесь, что нет конфликтов
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // implementation("io.ktor:ktor-client-android:2.3.7") // Используйте Android engine
}
```

### 3. Настройте Kotlin Coroutines

**app/build.gradle.kts:**
```kotlin
android {
    // ...
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

## Использование в Android

### 1. Создание клиента в ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow<McpState>(McpState.Idle)
    val state: StateFlow<McpState> = _state
    
    private lateinit var mcpClient: McpClient
    
    fun initializeMcp(apiKey: String) {
        viewModelScope.launch {
            try {
                _state.value = McpState.Loading
                
                val config = McpConfig(
                    url = "https://mcp.context7.com/mcp",
                    headers = mapOf("CONTEXT7_API_KEY" to apiKey)
                )
                
                mcpClient = McpClient(config)
                mcpClient.initialize()
                
                _state.value = McpState.Success("Connected")
            } catch (e: Exception) {
                _state.value = McpState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun callTool(toolName: String, arguments: kotlinx.serialization.json.JsonObject?) {
        viewModelScope.launch {
            try {
                _state.value = McpState.Loading
                
                val result = mcpClient.callTool(toolName, arguments)
                _state.value = McpState.ToolResult(result)
            } catch (e: Exception) {
                _state.value = McpState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (::mcpClient.isInitialized) {
            mcpClient.close()
        }
    }
}

sealed class McpState {
    object Idle : McpState()
    object Loading : McpState()
    data class Success(val message: String) : McpState()
    data class Error(val message: String) : McpState()
    data class ToolResult(val result: org.example.mcp.models.CallToolResult) : McpState()
}
```

### 2. Использование в Activity/Fragment (Compose)

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                McpClientScreen()
            }
        }
    }
}

@Composable
fun McpClientScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var apiKey by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // API Key input
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Connect button
        Button(
            onClick = { viewModel.initializeMcp(apiKey) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status
        when (val currentState = state) {
            is McpState.Idle -> Text("Not connected")
            is McpState.Loading -> CircularProgressIndicator()
            is McpState.Success -> Text("Success: ${currentState.message}")
            is McpState.Error -> Text("Error: ${currentState.message}", color = MaterialTheme.colorScheme.error)
            is McpState.ToolResult -> {
                Text("Tool result received")
                // Обработайте результат
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Example tool call
        Button(
            onClick = {
                val arguments = buildJsonObject {
                    put("libraryName", "react")
                    put("query", "How to use hooks?")
                }
                viewModel.callTool("resolve-library-id", arguments)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Call Tool")
        }
    }
}
```

### 3. Использование в Activity/Fragment (традиционный XML)

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Наблюдение за состоянием
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is McpState.Loading -> showLoading()
                    is McpState.Success -> showSuccess(state.message)
                    is McpState.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
        
        // Инициализация клиента
        val apiKey = "your_api_key"
        viewModel.initializeMcp(apiKey)
    }
}
```

## Хранение API ключей

### Вариант 1: local.properties (для разработки)

**local.properties:**
```properties
CONTEXT7_API_KEY=your_api_key_here
```

**app/build.gradle.kts:**
```kotlin
android {
    defaultConfig {
        // Загрузить из local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        
        buildConfigField(
            "String",
            "CONTEXT7_API_KEY",
            "\"${localProperties.getProperty("CONTEXT7_API_KEY", "")}\""
        )
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

**В коде:**
```kotlin
val apiKey = BuildConfig.CONTEXT7_API_KEY
viewModel.initializeMcp(apiKey)
```

### Вариант 2: Encrypted SharedPreferences (для production)

```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString("api_key", apiKey).apply()
    }
    
    fun getApiKey(): String? {
        return sharedPreferences.getString("api_key", null)
    }
}
```

## Разрешения

Добавьте в **AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## ProGuard Rules

Если используете ProGuard/R8, добавьте в **proguard-rules.pro:**

```proguard
# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.example.mcp.**$$serializer { *; }
-keepclassmembers class org.example.mcp.** {
    *** Companion;
}
-keepclasseswithmembers class org.example.mcp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# MCP Client
-keep class org.example.mcp.** { *; }
```

## Устранение конфликтов зависимостей

Если возникают конфликты с Ktor или Coroutines:

**app/build.gradle.kts:**
```kotlin
dependencies {
    implementation("com.github.germanbakunov:McpClient:1.0.0") {
        // Исключите конфликтующие зависимости
        exclude(group = "io.ktor", module = "ktor-client-core")
    }
    
    // Добавьте нужную версию вручную
    implementation("io.ktor:ktor-client-android:2.3.7")
}
```

## Использование Android-специфичного Ktor клиента

По умолчанию MCP Client использует CIO engine. Для Android рекомендуется использовать Android engine:

```kotlin
// Создайте кастомный HttpClient с Android engine
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val androidHttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
}

// Используйте этот клиент в MCP Client
// (потребуется модификация McpClient для принятия кастомного HttpClient)
```

## Обработка lifecycle

Всегда закрывайте клиент при уничтожении компонента:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    if (::mcpClient.isInitialized) {
        mcpClient.close()
    }
}
```

## Тестирование

### Unit тесты

```kotlin
class McpViewModelTest {
    @Test
    fun `test MCP initialization`() = runTest {
        val viewModel = MainViewModel()
        viewModel.initializeMcp("test_api_key")
        
        // Проверьте состояние
        val state = viewModel.state.first()
        assertTrue(state is McpState.Success || state is McpState.Error)
    }
}
```

## Полный пример проекта

См. пример Android приложения: [examples/android-app](https://github.com/germanbakunov/McpClient/tree/main/examples/android-app) (если доступно)

## Troubleshooting

### Ошибка "Unable to resolve dependency"

Убедитесь, что JitPack репозиторий добавлен в `settings.gradle.kts`, а не в `build.gradle.kts` модуля.

### Ошибка SSL/TLS

Убедитесь, что в `AndroidManifest.xml` разрешен cleartext traffic (только для разработки):

```xml
<application
    android:usesCleartextTraffic="true">
```

### Конфликты версий Kotlin

Убедитесь, что версии Kotlin совпадают:

```kotlin
plugins {
    kotlin("android") version "2.2.21"
}
```

## Дополнительные ресурсы

- [Kotlin Coroutines на Android](https://developer.android.com/kotlin/coroutines)
- [Ktor Client для Android](https://ktor.io/docs/client.html)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
