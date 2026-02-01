# Публикация MCP Client

Этот документ описывает, как публиковать и использовать библиотеку MCP Client в других проектах.

## Публикация через JitPack (Рекомендуется)

JitPack - это самый простой способ опубликовать библиотеку. Он автоматически собирает и публикует вашу библиотеку из GitHub репозитория.

### Шаги для публикации:

1. **Убедитесь, что код запушен на GitHub**
   ```bash
   git add .
   git commit -m "Prepare for publishing"
   git push origin main
   ```

2. **Создайте release на GitHub**
   - Перейдите на страницу вашего репозитория на GitHub
   - Нажмите "Releases" → "Create a new release"
   - Создайте тег версии (например, `v1.0.0`)
   - Опубликуйте release

3. **JitPack автоматически соберет вашу библиотеку**
   - Откройте https://jitpack.io
   - Введите ваш GitHub URL: `germanbakunov/McpClient`
   - JitPack автоматически соберет библиотеку
   - Вы получите badge со статусом сборки

## Использование в других Android/Kotlin проектах

### Через JitPack

В вашем проекте добавьте JitPack репозиторий:

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

Затем добавьте зависимость в ваш модуль:

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

### Использование последней версии

Вместо конкретной версии можно использовать:
- `main-SNAPSHOT` - последний коммит из main ветки
- `develop-SNAPSHOT` - последний коммит из develop ветки
- `v1.0.0` или `1.0.0` - конкретный release

```kotlin
implementation("com.github.germanbakunov:McpClient:main-SNAPSHOT")
```

## Публикация в Maven Central (Продвинутый вариант)

Если вы хотите опубликовать в Maven Central для более широкого распространения:

### Предварительные требования:

1. **Зарегистрируйтесь в Sonatype OSSRH**
   - Создайте аккаунт на https://issues.sonatype.org
   - Создайте issue для вашего groupId (io.github.germanbakunov)
   - Подтвердите владение доменом/GitHub аккаунтом

2. **Создайте GPG ключи для подписи**
   ```bash
   # Создать ключ
   gpg --gen-key
   
   # Экспортировать публичный ключ
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   
   # Получить key ID
   gpg --list-keys
   ```

3. **Настройте gradle.properties**
   
   Создайте или обновите `~/.gradle/gradle.properties`:
   ```properties
   signing.keyId=YOUR_KEY_ID
   signing.password=YOUR_GPG_PASSWORD
   signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
   
   ossrhUsername=YOUR_SONATYPE_USERNAME
   ossrhPassword=YOUR_SONATYPE_PASSWORD
   ```

### Публикация в Maven Central:

```bash
# Соберите и опубликуйте
./gradlew publishMavenPublicationToOSSRHRepository

# Закройте и release staging репозиторий в Sonatype UI
# Или используйте Gradle Nexus Publish Plugin для автоматизации
```

После успешной публикации в Maven Central, пользователи смогут использовать библиотеку без дополнительных репозиториев:

```kotlin
dependencies {
    implementation("io.github.germanbakunov:mcp-client:1.0.0")
}
```

## Локальное тестирование

Перед публикацией можно протестировать локально:

```bash
# Опубликовать в локальный Maven репозиторий
./gradlew publishToMavenLocal

# Затем в другом проекте используйте
repositories {
    mavenLocal()
}

dependencies {
    implementation("io.github.germanbakunov:mcp-client:1.0.0")
}
```

## Версионирование

Используйте семантическое версионирование (Semantic Versioning):
- **MAJOR** (1.x.x) - несовместимые изменения API
- **MINOR** (x.1.x) - новая функциональность с обратной совместимостью
- **PATCH** (x.x.1) - исправления ошибок

Обновляйте версию в `build.gradle.kts`:
```kotlin
version = "1.0.0"
```

## Troubleshooting

### JitPack не может собрать проект

1. Проверьте https://jitpack.io/#germanbakunov/McpClient для логов сборки
2. Убедитесь, что `jitpack.yml` настроен правильно
3. Проверьте, что проект собирается локально: `./gradlew clean build`

### Проблемы с зависимостями в Android

Если возникают конфликты зависимостей, добавьте исключения:
```kotlin
implementation("com.github.germanbakunov:McpClient:1.0.0") {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
}
```

## Дополнительные ресурсы

- [JitPack Documentation](https://jitpack.io/docs/)
- [Maven Central Publishing Guide](https://central.sonatype.org/publish/)
- [Semantic Versioning](https://semver.org/)
