# 🎉 Библиотека успешно опубликована на JitPack!

## ✅ Информация о публикации

- **Репозиторий:** https://github.com/bakunovgerman/McpClient
- **JitPack:** https://jitpack.io/#bakunovgerman/McpClient
- **Версия:** v1.1.0
- **Координаты:** `com.github.bakunovgerman:McpClient:v1.1.0`

## 🚀 Использование в проектах

### 1. Android проект

#### settings.gradle.kts
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### app/build.gradle.kts
```kotlin
dependencies {
    // MCP Client
    implementation("com.github.bakunovgerman:McpClient:v1.1.0")
    
    // Если нужна последняя версия из main
    // implementation("com.github.bakunovgerman:McpClient:main-SNAPSHOT")
}
```

### 2. Обычный Kotlin/JVM проект

#### settings.gradle.kts
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### build.gradle.kts
```kotlin
dependencies {
    implementation("com.github.bakunovgerman:McpClient:v1.1.0")
}
```

## 💡 Примеры использования

### Простой пример

```kotlin
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf("CONTEXT7_API_KEY" to "your_api_key")
    )
    
    val client = McpClient(config)
    
    try {
        client.initialize()
        val tools = client.listTools()
        
        tools.tools.forEach { tool ->
            println("${tool.name}: ${tool.description}")
        }
    } finally {
        client.close()
    }
}
```

### В Android ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

class MyViewModel : ViewModel() {
    private lateinit var mcpClient: McpClient
    
    fun connect(apiKey: String) {
        viewModelScope.launch {
            val config = McpConfig(
                url = "https://mcp.context7.com/mcp",
                headers = mapOf("CONTEXT7_API_KEY" to apiKey)
            )
            
            mcpClient = McpClient(config)
            mcpClient.initialize()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (::mcpClient.isInitialized) {
            mcpClient.close()
        }
    }
}
```

## 📦 Доступные версии

Вы можете использовать разные версии:

```kotlin
// Конкретная версия (рекомендуется для production)
implementation("com.github.bakunovgerman:McpClient:v1.1.0")
implementation("com.github.bakunovgerman:McpClient:1.1.0") // без префикса v

// Последняя версия из main (для разработки/тестирования)
implementation("com.github.bakunovgerman:McpClient:main-SNAPSHOT")

// Конкретный коммит
implementation("com.github.bakunovgerman:McpClient:4b6a8ab")
```

## 🔄 Обновление до новой версии

Когда выпустите новую версию:

1. **Обновите версию в `build.gradle.kts`:**
   ```kotlin
   version = "1.2.0"
   ```

2. **Закоммитьте и создайте новый release:**
   ```bash
   git add .
   git commit -m "Version 1.2.0"
   git tag v1.2.0
   git push origin main --tags
   ```

3. **JitPack автоматически соберет новую версию**

4. **Пользователи обновят зависимость:**
   ```kotlin
   implementation("com.github.bakunovgerman:McpClient:v1.2.0")
   ```

## 🌟 Добавьте badge в README

Обновите ваш README.md:

```markdown
[![](https://jitpack.io/v/bakunovgerman/McpClient.svg)](https://jitpack.io/#bakunovgerman/McpClient)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
```

Это покажет текущую версию и статус сборки.

## 📚 Дополнительная документация

Для подробных примеров смотрите:
- [USAGE_EXAMPLE.md](USAGE_EXAMPLE.md) - Примеры использования
- [ANDROID_USAGE.md](ANDROID_USAGE.md) - Android специфика
- [EXAMPLES.md](EXAMPLES.md) - Расширенные примеры

## 🐛 Troubleshooting

### Не находит зависимость

Убедитесь, что JitPack репозиторий добавлен в `settings.gradle.kts` (не в `build.gradle.kts` модуля):

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Конфликты зависимостей

Если возникают конфликты с Ktor или Coroutines:

```kotlin
implementation("com.github.bakunovgerman:McpClient:v1.1.0") {
    exclude(group = "io.ktor", module = "ktor-client-core")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
}
```

### Sync failed

1. Очистите Gradle cache:
   ```bash
   ./gradlew clean --refresh-dependencies
   ```

2. Инвалидируйте кеш в Android Studio:
   - File → Invalidate Caches → Invalidate and Restart

3. Подождите 5-10 минут после создания release (JitPack может кешировать)

## 📊 Статистика использования

Проверить статистику загрузок можно на:
https://jitpack.io/#bakunovgerman/McpClient

## 🎯 Следующие шаги

1. ✅ Поделитесь библиотекой с командой
2. ✅ Добавьте примеры в README
3. ✅ Создайте Wiki на GitHub
4. ✅ Добавьте больше тестов
5. ✅ Соберите feedback от пользователей

## 🙏 Спасибо за использование!

Если библиотека полезна, поставьте ⭐ на GitHub!

---

**Важно:** Я также исправил warning о `ExperimentalSerializationApi` в коде. Закоммитьте изменения для следующей версии:

```bash
git add .
git commit -m "Fix ExperimentalSerializationApi warning"
git push origin main
```

Создайте новый release `v1.1.1` для публикации исправления.
