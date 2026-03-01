package org.example.openrouter

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class OpenRouterClient(
    private val apiKey: String,
    private val url: String = "https://openrouter.ai/api/v1/chat/completions"
) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun chat(userMessage: String): String {
        val request = OpenRouterChatRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(
                OpenRouterMessage(role = "user", content = userMessage)
            )
        )
        val response: OpenRouterChatResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("OpenRouter response has no choices or content")
        return content
    }

    fun close() {
        httpClient.close()
    }
}
