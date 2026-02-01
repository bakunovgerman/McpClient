# 🔧 Установка и настройка

## Требования

### Java 24

Проект требует Java 24 или выше. Проверьте версию Java:

```bash
java -version
```

## Установка Java 24

### macOS

#### Вариант 1: SDKMAN (Рекомендуется)

```bash
# Установите SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Установите Java 24
sdk install java 24-open

# Используйте Java 24 по умолчанию
sdk default java 24-open

# Проверьте
java -version
```

#### Вариант 2: Homebrew

```bash
# Установите Homebrew (если еще не установлен)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Установите OpenJDK 24
brew install openjdk@24

# Добавьте в PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@24/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Проверьте
java -version
```

#### Вариант 3: Oracle JDK

Скачайте с официального сайта Oracle:
https://www.oracle.com/java/technologies/downloads/

### Linux

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-24-jdk

# Fedora/RHEL
sudo dnf install java-24-openjdk

# Arch Linux
sudo pacman -S jdk-openjdk
```

### Windows

1. Скачайте JDK 24 с:
   - https://www.oracle.com/java/technologies/downloads/
   - или https://adoptium.net/

2. Запустите установщик

3. Добавьте в PATH:
   - Панель управления → Система → Дополнительные параметры системы
   - Переменные среды
   - Добавьте путь к Java в PATH

## Проверка установки

После установки Java:

```bash
# Проверьте Java
java -version
# Должно показать: openjdk version "24..." или выше

# Проверьте Gradle (через wrapper)
cd /Users/germanbakunov/Desktop/day11/day11
./gradlew --version
```

## Настройка проекта

### 1. API ключ Context7

Получите API ключ:
1. Посетите https://context7.com
2. Зарегистрируйтесь или войдите
3. Получите API ключ

### 2. Установите переменную окружения

#### Временно (для текущей сессии)

```bash
export CONTEXT7_API_KEY="your_api_key_here"
```

#### Постоянно

##### macOS/Linux (zsh)

```bash
echo 'export CONTEXT7_API_KEY="your_api_key_here"' >> ~/.zshrc
source ~/.zshrc
```

##### macOS/Linux (bash)

```bash
echo 'export CONTEXT7_API_KEY="your_api_key_here"' >> ~/.bashrc
source ~/.bashrc
```

##### Windows

```powershell
# PowerShell (временно)
$env:CONTEXT7_API_KEY = "your_api_key_here"

# Постоянно через System Properties
setx CONTEXT7_API_KEY "your_api_key_here"
```

### 3. Проверьте настройку

```bash
echo $CONTEXT7_API_KEY
# Должен вывести ваш API ключ
```

## Сборка проекта

```bash
cd /Users/germanbakunov/Desktop/day11/day11

# Очистка и сборка
./gradlew clean build

# Если видите ошибки сборки, попробуйте:
./gradlew clean build --refresh-dependencies
```

## Запуск

### Основной пример

```bash
./gradlew run
```

### Другие примеры

```bash
# Базовый пример
./run-basic.sh

# Вызов инструментов
./run-tool-call.sh

# Batch операции
./run-batch.sh

# Интерактивный CLI
./run-interactive.sh
```

### Запуск конкретного примера

```bash
./gradlew run -PmainClass=org.example.examples.BasicExampleKt
./gradlew run -PmainClass=org.example.examples.ToolCallExampleKt
./gradlew run -PmainClass=org.example.examples.BatchOperationsExampleKt
./gradlew run -PmainClass=org.example.examples.InteractiveCliKt
```

## Устранение проблем

### Проблема: "Unable to locate a Java Runtime"

**Решение:** Установите Java 24 (см. выше)

### Проблема: Gradle не запускается

```bash
# Дайте права на выполнение
chmod +x gradlew

# Попробуйте снова
./gradlew --version
```

### Проблема: "CONTEXT7_API_KEY not found"

**Решение:**
```bash
# Убедитесь что переменная установлена
echo $CONTEXT7_API_KEY

# Если пусто, установите:
export CONTEXT7_API_KEY="your_key"
```

### Проблема: Ошибки сборки

```bash
# Очистите кеш Gradle
./gradlew clean --refresh-dependencies

# Удалите .gradle директорию
rm -rf .gradle

# Пересоберите
./gradlew build
```

### Проблема: Ошибки подключения к серверу

1. Проверьте интернет соединение
2. Проверьте правильность API ключа
3. Проверьте доступность сервера:
   ```bash
   curl -I https://mcp.context7.com/mcp
   ```

### Проблема: Скрипты не запускаются

```bash
# Дайте права на выполнение всем скриптам
chmod +x run-*.sh

# Запустите
./run-basic.sh
```

## Дополнительная настройка

### IDE Setup (IntelliJ IDEA)

1. Откройте проект в IntelliJ IDEA
2. File → Project Structure → Project SDK → Выберите Java 24
3. Gradle автоматически импортирует зависимости

### VS Code Setup

1. Установите расширения:
   - Kotlin Language Support
   - Gradle for Java

2. Откройте проект
3. VS Code автоматически определит Gradle проект

### Логирование

Настройте уровень логирования в `src/main/resources/logback.xml`:

```xml
<logger name="io.ktor" level="DEBUG" />  <!-- Для подробных логов -->
<logger name="io.ktor" level="INFO" />   <!-- Для стандартных логов -->
<logger name="io.ktor" level="ERROR" />  <!-- Только ошибки -->
```

## Тестирование

```bash
# Запуск всех тестов
./gradlew test

# Запуск с подробным выводом
./gradlew test --info

# Запуск конкретного теста
./gradlew test --tests "org.example.mcp.McpClientTest"
```

## Проверка готовности

Выполните эти команды для проверки:

```bash
# 1. Java установлена?
java -version
# ✓ Должна быть версия 24+

# 2. Gradle работает?
./gradlew --version
# ✓ Должен показать версию Gradle

# 3. API ключ установлен?
echo $CONTEXT7_API_KEY
# ✓ Должен показать ваш ключ

# 4. Проект собирается?
./gradlew build
# ✓ BUILD SUCCESSFUL

# 5. Проект запускается?
./gradlew run
# ✓ Должен подключиться к серверу
```

Если все команды выполнены успешно - вы готовы к работе! 🎉

## Полезные команды

```bash
# Очистка
./gradlew clean

# Сборка без тестов
./gradlew build -x test

# Список всех задач
./gradlew tasks

# Зависимости
./gradlew dependencies

# Помощь
./gradlew help
```

## Следующие шаги

После успешной установки:

1. Прочитайте [QUICKSTART.md](QUICKSTART.md)
2. Изучите [EXAMPLES.md](EXAMPLES.md)
3. Посмотрите [README.md](README.md)
4. Попробуйте примеры в `src/main/kotlin/org/example/examples/`
