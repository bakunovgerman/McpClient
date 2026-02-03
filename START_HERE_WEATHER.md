# 🎯 НАЧНИТЕ ОТСЮДА - Weather AI Server

> Добро пожаловать! Это ваша отправная точка для работы с Weather AI Server.

---

## 📌 Что это?

**Weather AI Server** - Kotlin приложение, которое:
- 🤖 Использует AI (GPT-4o-mini) для запроса погоды
- ⏱️ Автоматически обновляет данные каждую минуту
- 💾 Сохраняет всю историю в SQL базу данных
- 🌐 Предоставляет REST API для доступа к данным

---

## 🚀 Быстрый старт (5 минут)

### Шаг 1: Получите OpenRouter API ключ

1. Откройте https://openrouter.ai/
2. Зарегистрируйтесь (если еще не зарегистрированы)
3. Перейдите в **Keys**: https://openrouter.ai/keys
4. Нажмите **Create Key**
5. Скопируйте ключ (начинается с `sk-or-v1-...`)
6. Пополните баланс минимум на $5

### Шаг 2: Настройте проект

```bash
cd McpClient

# Создайте файл с переменными окружения
cp env.example .env

# Отредактируйте .env и вставьте ваш API ключ
nano .env
```

В файле `.env` замените:
```
OPENROUTER_API_KEY=your_openrouter_api_key_here
```

на ваш ключ:
```
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxx
```

Сохраните и закройте файл (Ctrl+O, Enter, Ctrl+X).

### Шаг 3: Запустите сервер

```bash
# Дайте права на выполнение
chmod +x build-and-run.sh

# Запустите (автоматически соберет и запустит)
./build-and-run.sh
```

### Шаг 4: Проверьте работу

Откройте новый терминал и выполните:

```bash
# Проверка здоровья
curl http://localhost:8080/health
# Ответ: OK

# Получить последние записи о погоде
curl http://localhost:8080/weather/latest

# Запросить погоду немедленно
curl http://localhost:8080/weather/check-now
```

---

## 🎉 Готово!

Сервер работает! Каждую минуту он автоматически запрашивает погоду в Москве через AI.

### Что происходит?

1. ⏱️ **Каждую минуту**: Сервер запрашивает погоду
2. 🤖 **AI генерирует**: GPT-4o-mini создает описание погоды
3. 💾 **Сохранение**: Ответ сохраняется в базу данных
4. 📝 **Логирование**: Все операции записываются в логи

### Просмотр логов

```bash
# В реальном времени
tail -f logs/weather-agent.log

# Последние 50 строк
tail -n 50 logs/weather-agent.log
```

---

## 📚 Что дальше?

### Для новичков

1. **[QUICKSTART.ru.md](QUICKSTART.ru.md)** - Подробный быстрый старт
2. **[FAQ.md](FAQ.md)** - Ответы на частые вопросы
3. **[TEST_API.md](TEST_API.md)** - Как тестировать API

### Для разработчиков

1. **[README.WEATHER.md](README.WEATHER.md)** - Обзор проекта
2. **[README_WEATHER_SERVER.md](README_WEATHER_SERVER.md)** - Полная документация
3. **[PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)** - Технический обзор

### Для деплоя

1. **[DEPLOYMENT.md](DEPLOYMENT.md)** - Развертывание на VPS
2. **[DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)** - Развертывание с Docker
3. **[DATABASE_GUIDE.md](DATABASE_GUIDE.md)** - Работа с базой данных

---

## 🗺️ Карта документации

```
START_HERE_WEATHER.md (ВЫ ЗДЕСЬ)
│
├─ Быстрый старт
│  ├─ QUICKSTART.ru.md .............. Подробный быстрый старт
│  └─ build-and-run.sh .............. Скрипт автоматического запуска
│
├─ Основная документация
│  ├─ README.WEATHER.md ............. Главная страница проекта
│  ├─ README_WEATHER_SERVER.md ...... Полная документация
│  └─ PROJECT_OVERVIEW.md ........... Технический обзор
│
├─ Развертывание
│  ├─ DEPLOYMENT.md ................. Развертывание на VPS
│  ├─ DOCKER_DEPLOYMENT.md .......... Docker развертывание
│  └─ weather-ai.service ............ Systemd unit файл
│
├─ База данных
│  └─ DATABASE_GUIDE.md ............. Работа с БД
│
├─ Тестирование
│  └─ TEST_API.md ................... Тестирование API
│
└─ Поддержка
   └─ FAQ.md ........................ Часто задаваемые вопросы
```

---

## 🛠️ Основные команды

### Запуск и остановка

```bash
# Запуск (сборка + запуск)
./build-and-run.sh

# Или просто запуск (если уже собран)
./run-weather-server.sh

# Остановка - несколько способов:

# 1. Если запущен в терминале - нажмите:
Ctrl+C

# 2. Если запущен в фоне - используйте скрипт:
./stop-weather-server.sh

# 3. Или найти и остановить вручную:
ps aux | grep "McpClient-1.0-SNAPSHOT.jar"
kill PID  # замените PID на номер процесса
```

📖 **Подробнее об остановке**: см. [HOW_TO_STOP.md](HOW_TO_STOP.md)

### Проверка работы

```bash
# Health check
curl http://localhost:8080/health

# Последние записи
curl http://localhost:8080/weather/latest | jq '.'

# Все записи
curl http://localhost:8080/weather/all | jq '.'

# Ручной запрос погоды
curl http://localhost:8080/weather/check-now
```

### Просмотр логов

```bash
# В реальном времени
tail -f logs/weather-agent.log

# Поиск ошибок
grep ERROR logs/weather-agent.log

# Последние записи о погоде
grep "Ответ LLM о погоде" logs/weather-agent.log
```

---

## 🆘 Помощь

### Сервер не запускается?

1. **Проверьте Java**:
   ```bash
   java -version
   # Должна быть версия 17 или выше
   ```

2. **Проверьте API ключ**:
   ```bash
   cat .env | grep OPENROUTER_API_KEY
   # Должен быть установлен ваш ключ
   ```

3. **Проверьте логи**:
   ```bash
   tail -f logs/weather-agent.log
   ```

### Другие проблемы?

- Смотрите **[FAQ.md](FAQ.md)** для частых вопросов
- Проверьте документацию в соответствующем разделе
- Создайте Issue на GitHub

---

## 📊 API Endpoints

| Endpoint | Метод | Описание |
|----------|-------|----------|
| `/` | GET | Главная страница |
| `/health` | GET | Проверка здоровья |
| `/weather/latest` | GET | Последние 10 записей |
| `/weather/all` | GET | Все записи |
| `/weather/check-now` | GET | Ручной запрос погоды |

---

## 🎓 Что вы получите

После запуска у вас будет:

✅ **Рабочий сервер** на порту 8080  
✅ **Автоматические запросы** погоды каждую минуту  
✅ **База данных** со всей историей  
✅ **REST API** для доступа к данным  
✅ **Подробные логи** всех операций  
✅ **Health checks** для мониторинга  

---

## 💡 Совет

Оставьте сервер работать несколько минут, чтобы накопилось несколько записей о погоде. Затем посмотрите историю:

```bash
curl http://localhost:8080/weather/all | jq '.'
```

---

## 🌟 Готовы к большему?

### Варианты развертывания

- **Локально**: Уже работает! ✅
- **VPS**: См. [DEPLOYMENT.md](DEPLOYMENT.md)
- **Docker**: См. [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)

### Расширение функционала

- Добавьте другие города
- Создайте Telegram бота
- Сделайте веб-интерфейс
- Настройте алерты
- Добавьте графики и аналитику

Идеи в **[FAQ.md](FAQ.md)** → раздел "Дополнительные возможности"

---

## 📞 Контакты

- **OpenRouter**: https://openrouter.ai/
- **OpenRouter Docs**: https://openrouter.ai/docs
- **Ktor Framework**: https://ktor.io/

---

## ✨ Поздравляем!

Вы запустили Weather AI Server! 🎉

Теперь у вас есть свой AI-агент, который мониторит погоду 24/7.

**Следующий шаг**: Прочитайте [README.WEATHER.md](README.WEATHER.md) для полного понимания возможностей.

---

**Удачи! Пусть погода всегда будет на вашей стороне! ☀️🌧️⛈️**
