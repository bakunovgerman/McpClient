# ✅ Checklist для публикации на JitPack

Используйте этот checklist перед публикацией вашей библиотеки.

## Перед первой публикацией

- [ ] **Обновите персональные данные в `build.gradle.kts`:**
  - [ ] `group = "io.github.YOUR_GITHUB_USERNAME"`
  - [ ] URL репозитория в `pom.url`
  - [ ] Имя разработчика в `developers.developer.name`
  - [ ] Email разработчика в `developers.developer.email`
  - [ ] GitHub username в `developers.developer.id`
  - [ ] SCM URLs (connection, developerConnection, url)

- [ ] **Проверьте версию в `build.gradle.kts`:**
  - [ ] `version = "1.0.0"` (или вашу версию)
  - [ ] Удалите `-SNAPSHOT` для release версий

- [ ] **Создайте или обновите документацию:**
  - [ ] README.md с примерами использования
  - [ ] CHANGELOG.md с описанием изменений
  - [ ] LICENSE файл (MIT уже создан)
  - [ ] Badge в README.md обновлен с вашим GitHub username

- [ ] **Локальное тестирование:**
  - [ ] Проект собирается локально: `./gradlew clean build`
  - [ ] Тесты проходят: `./gradlew test`
  - [ ] Публикация в Maven Local работает: `./gradlew publishToMavenLocal`
  - [ ] Проверили артефакты в `~/.m2/repository/`

- [ ] **Git репозиторий настроен:**
  - [ ] Код залит на GitHub
  - [ ] Репозиторий публичный (не private)
  - [ ] .gitignore настроен правильно
  - [ ] Все изменения закоммичены

## Публикация на JitPack

- [ ] **Создайте Git tag:**
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```
  
- [ ] **Или создайте GitHub Release:**
  - [ ] Перейдите на страницу Releases
  - [ ] Нажмите "Create a new release"
  - [ ] Tag version: `v1.0.0` (или `1.0.0`)
  - [ ] Release title: "v1.0.0" или описательное название
  - [ ] Описание: скопируйте из CHANGELOG.md
  - [ ] Нажмите "Publish release"

- [ ] **Проверьте сборку на JitPack:**
  - [ ] Откройте https://jitpack.io
  - [ ] Введите `YOUR_USERNAME/McpClient`
  - [ ] Нажмите "Look up"
  - [ ] Дождитесь зеленого badge (может занять 1-5 минут)
  - [ ] Если ошибка - проверьте логи сборки

## После публикации

- [ ] **Проверьте использование:**
  ```kotlin
  // В тестовом проекте
  implementation("com.github.YOUR_USERNAME:McpClient:1.0.0")
  ```

- [ ] **Обновите документацию:**
  - [ ] Badge в README показывает актуальную версию
  - [ ] Примеры использования обновлены с актуальной версией
  - [ ] USAGE_EXAMPLE.md обновлен

- [ ] **Сообщите пользователям:**
  - [ ] Анонсируйте в README или Discord/Slack
  - [ ] Обновите документацию проектов, использующих библиотеку

## Для каждой новой версии

- [ ] **Обновите версию:**
  - [ ] Увеличьте версию в `build.gradle.kts`
  - [ ] Следуйте Semantic Versioning (MAJOR.MINOR.PATCH)

- [ ] **Обновите CHANGELOG.md:**
  - [ ] Добавьте секцию для новой версии
  - [ ] Опишите изменения (Added, Changed, Fixed, etc.)

- [ ] **Тестирование:**
  - [ ] Все тесты проходят
  - [ ] Проект собирается без ошибок
  - [ ] Backward compatibility проверена (если MINOR/PATCH)

- [ ] **Создайте новый release:**
  - [ ] Закоммитьте изменения
  - [ ] Создайте новый tag/release
  - [ ] JitPack автоматически соберет новую версию

## Troubleshooting

### ❌ JitPack не может собрать проект

**Решение:**
1. Проверьте логи на JitPack (кликните на версию)
2. Убедитесь, что `jitpack.yml` настроен правильно
3. Проверьте, что проект собирается локально
4. Убедитесь, что используется Java 24

### ❌ "Could not find..." при добавлении зависимости

**Решение:**
1. Проверьте, что JitPack репозиторий добавлен в `settings.gradle.kts`
2. Подождите 5-10 минут после создания release
3. Очистите Gradle cache: `./gradlew clean --refresh-dependencies`
4. Проверьте правильность groupId: `com.github.YOUR_USERNAME:McpClient:VERSION`

### ❌ Конфликты зависимостей

**Решение:**
```kotlin
implementation("com.github.YOUR_USERNAME:McpClient:1.0.0") {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
}
```

### ❌ Badge не показывается в README

**Решение:**
1. Убедитесь, что GitHub username в badge правильный
2. Проверьте, что репозиторий публичный
3. Подождите несколько минут после первого release

## Дополнительные ресурсы

- [QUICK_PUBLISH_GUIDE.md](QUICK_PUBLISH_GUIDE.md) - Подробное руководство
- [PUBLISHING.md](PUBLISHING.md) - Детали публикации
- [JitPack Documentation](https://jitpack.io/docs/)
- [Semantic Versioning](https://semver.org/)

---

**Готово?** Начните с [QUICK_PUBLISH_GUIDE.md](QUICK_PUBLISH_GUIDE.md) для пошаговых инструкций! 🚀
