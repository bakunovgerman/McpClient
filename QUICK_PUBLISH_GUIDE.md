# 🚀 Быстрое руководство по публикации

Этот документ содержит пошаговую инструкцию для публикации вашего MCP клиента через JitPack.

## ✅ Что уже сделано

- ✅ Настроен `maven-publish` plugin
- ✅ Добавлены POM метаданные
- ✅ Создан `jitpack.yml` для конфигурации сборки
- ✅ Настроена публикация sources и javadoc
- ✅ Добавлен GitHub Actions workflow
- ✅ Создана документация

## 📋 Что нужно сделать

### 1. Обновите персональные данные

Откройте `build.gradle.kts` и замените следующие значения:

```kotlin
group = "io.github.germanbakunov" // Замените на ваш GitHub username

pom {
    url.set("https://github.com/bakunovgerman/McpClient") // Ваш GitHub URL
    
    developers {
        developer {
            id.set("germanbakunov") // Ваш GitHub username
            name.set("German Bakunov") // Ваше имя
            email.set("your.email@example.com") // Ваш email
        }
    }
    
    scm {
        connection.set("scm:git:git://github.com/bakunovgerman/McpClient.git")
        developerConnection.set("scm:git:ssh://github.com/bakunovgerman/McpClient.git")
        url.set("https://github.com/bakunovgerman/McpClient")
    }
}
```

### 2. Создайте GitHub репозиторий (если еще не создан)

```bash
# Перейдите на https://github.com/new
# Создайте публичный репозиторий с названием "McpClient"
# НЕ инициализируйте с README, .gitignore или лицензией
```

### 3. Запушьте код на GitHub

```bash
# Если репозиторий еще не настроен
git remote add origin https://github.com/bakunovgerman/McpClient.git

# Или обновите remote
git remote set-url origin https://github.com/bakunovgerman/McpClient.git

# Закоммитьте изменения
git add .
git commit -m "Configure publishing to JitPack"
git push -u origin main
```

### 4. Создайте первый release

**Через веб-интерфейс GitHub:**
1. Перейдите на страницу вашего репозитория
2. Нажмите "Releases" → "Create a new release"
3. В поле "Choose a tag" введите `v1.0.0` (или `1.0.0`)
4. Заголовок: "First Release v1.0.0"
5. Описание: скопируйте из `CHANGELOG.md`
6. Нажмите "Publish release"

**Или через командную строку:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

### 5. Проверьте публикацию на JitPack

1. Откройте https://jitpack.io
2. В поле поиска введите: `germanbakunov/McpClient`
3. Нажмите "Look up"
4. JitPack автоматически начнет сборку
5. Дождитесь зеленого badge "Get it" (может занять 1-5 минут)

### 6. Используйте библиотеку в других проектах

После успешной публикации на JitPack:

**В вашем Android/Kotlin проекте:**

**settings.gradle.kts:**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**app/build.gradle.kts:**
```kotlin
dependencies {
    implementation("com.github.germanbakunov:McpClient:1.0.0")
}
```

## 🔄 Обновление версии

Когда нужно выпустить новую версию:

1. **Обновите версию в `build.gradle.kts`:**
   ```kotlin
   version = "1.1.0" // Новая версия
   ```

2. **Обновите `CHANGELOG.md`:**
   ```markdown
   ## [1.1.0] - 2026-02-15
   
   ### Added
   - Новая функциональность
   
   ### Fixed
   - Исправленные баги
   ```

3. **Закоммитьте изменения:**
   ```bash
   git add .
   git commit -m "Bump version to 1.1.0"
   git push origin main
   ```

4. **Создайте новый release/tag:**
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```
   
   Или создайте release через GitHub UI.

5. **JitPack автоматически соберет новую версию**

## 🧪 Локальное тестирование

Перед публикацией протестируйте локально:

```bash
# Опубликовать в локальный Maven репозиторий
./gradlew publishToMavenLocal

# Проверьте, что файлы созданы
ls ~/.m2/repository/io/github/germanbakunov/mcp-client/1.0.0/
```

В тестовом проекте:
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.germanbakunov:mcp-client:1.0.0")
}
```

## 📊 Badge для README

После публикации добавьте badge в `README.md`:

```markdown
[![](https://jitpack.io/v/germanbakunov/McpClient.svg)](https://jitpack.io/#germanbakunov/McpClient)
```

Это покажет последнюю версию и статус сборки.

## ❓ Troubleshooting

### JitPack не может собрать проект

1. **Проверьте логи сборки на JitPack**
   - Кликните на версию → "Look at the build log"

2. **Убедитесь, что проект собирается локально:**
   ```bash
   ./gradlew clean build
   ```

3. **Проверьте `jitpack.yml`:**
   - Правильная версия JDK (24)
   - Правильные команды установки

### Ошибка "Could not find..."

- Подождите несколько минут после создания release
- Очистите Gradle cache: `./gradlew clean --refresh-dependencies`
- Проверьте правильность groupId и artifactId

### Конфликты зависимостей

Если в другом проекте возникают конфликты:
```kotlin
implementation("com.github.germanbakunov:McpClient:1.0.0") {
    exclude(group = "...", module = "...")
}
```

## 🎯 Следующие шаги

После успешной публикации:

1. ⭐ Попросите пользователей поставить звезду на GitHub
2. 📝 Добавьте примеры использования
3. 📚 Улучшите документацию
4. 🐛 Создайте Issues для отслеживания багов
5. 💡 Принимайте Pull Requests от сообщества

## 🌟 Альтернатива: Maven Central

Если хотите более широкое распространение, настройте публикацию в Maven Central:

См. подробности в [PUBLISHING.md](PUBLISHING.md) - раздел "Публикация в Maven Central"

---

**Поздравляем! 🎉** После выполнения этих шагов ваша библиотека будет доступна для использования в любом Gradle проекте!
