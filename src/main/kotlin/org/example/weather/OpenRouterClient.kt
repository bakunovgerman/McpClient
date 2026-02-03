package org.example.weather

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("OpenRouterClient")

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val message: Message,
    val finish_reason: String? = null,
    val index: Int = 0
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

class OpenRouterClient(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    org.example.weather.logger.debug("HTTP Client: $message")
                }
            }
        }
    }
    
    suspend fun sendChatCompletion(
        model: String = "openai/gpt-4o-mini",
        messages: List<ChatMessage>
    ): ChatCompletionResponse {
        logger.info("=== Отправка запроса к OpenRouter ===")
        logger.info("Модель: $model")
        logger.info("Сообщения: ${messages.size} шт.")
        messages.forEach { msg ->
            logger.info("  - ${msg.role}: ${msg.content}")
        }
        
        try {
            val response: ChatCompletionResponse = client.post("https://openrouter.ai/api/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "https://github.com/weather-ai-agent")
                header("X-Title", "Weather AI Agent")
                contentType(ContentType.Application.Json)
                
                setBody(ChatCompletionRequest(
                    model = model,
                    messages = messages
                ))
            }.body()
            
            logger.info("=== Получен ответ от OpenRouter ===")
            logger.info("ID: ${response.id}")
            logger.info("Модель: ${response.model}")
            response.choices.forEach { choice ->
                logger.info("Ответ [${choice.index}]: ${choice.message.content}")
            }
            response.usage?.let { usage ->
                logger.info("Использование токенов: prompt=${usage.prompt_tokens}, completion=${usage.completion_tokens}, total=${usage.total_tokens}")
            }
            
            return response
        } catch (e: Exception) {
            logger.error("Ошибка при запросе к OpenRouter", e)
            throw e
        }
    }
    
    fun close() {
        client.close()
    }
}
