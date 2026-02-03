package org.example.weather

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("WeatherAgent")

class WeatherAgent(private val openRouterClient: OpenRouterClient) {
    private val mutex = Mutex()
    
    suspend fun checkWeather() {
        mutex.withLock {
            logger.info("╔═══════════════════════════════════════════════════╗")
            logger.info("║   НАЧАЛО ПРОВЕРКИ ПОГОДЫ В МОСКВЕ                ║")
            logger.info("╚═══════════════════════════════════════════════════╝")
            
            try {
                val messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "Ты профессиональный метеоролог. Предоставляй актуальную информацию о погоде."
                    ),
                    ChatMessage(
                        role = "user",
                        content = "Какая сейчас погода в Москве? Укажи температуру, облачность, осадки и общее состояние погоды. Ответ дай на русском языке."
                    )
                )
                
                val response = openRouterClient.sendChatCompletion(
                    model = "openai/gpt-4o-mini",
                    messages = messages
                )
                
                val weatherText = response.choices.firstOrNull()?.message?.content
                    ?: throw IllegalStateException("Нет ответа от LLM")
                
                logger.info("=== Ответ LLM о погоде ===")
                logger.info(weatherText)
                
                // Сохранение в БД
                val recordId = insertWeatherRecord(
                    weatherResponse = weatherText,
                    modelUsed = response.model
                )
                
                logger.info("╔═══════════════════════════════════════════════════╗")
                logger.info("║   ПРОВЕРКА ПОГОДЫ ЗАВЕРШЕНА (ID: $recordId)         ")
                logger.info("╚═══════════════════════════════════════════════════╝")
                
            } catch (e: Exception) {
                logger.error("╔═══════════════════════════════════════════════════╗", e)
                logger.error("║   ОШИБКА ПРИ ПРОВЕРКЕ ПОГОДЫ                      ║", e)
                logger.error("╚═══════════════════════════════════════════════════╝", e)
            }
        }
    }
    
    suspend fun getLatestWeatherRecords(limit: Int = 10): List<WeatherRecord> {
        return getLatestWeatherRecords(limit)
    }
    
    suspend fun getAllWeatherRecords(): List<WeatherRecord> {
        return getAllWeatherRecords()
    }
}
