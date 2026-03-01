package org.example.openrouter

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.example.mcp.models.Tool

/**
 * Converts MCP inputSchema to OpenAI-compatible parameters.
 * Returns null when tool has no parameters.
 */
private fun mcpSchemaToOpenAIParameters(inputSchema: JsonObject): JsonObject? {
    val allowedKeys = setOf("type", "properties", "required")
    val filtered = inputSchema.filterKeys { it in allowedKeys }.toMutableMap()
    if (filtered.isEmpty() || !filtered.containsKey("properties")) return null
    if (!filtered.containsKey("type")) {
        filtered["type"] = JsonPrimitive("object")
    }
    return buildJsonObject { filtered.forEach { (k, v) -> put(k, v) } }
}

class OpenRouterClient(
    private val apiKey: String,
    private val url: String = "https://openrouter.ai/api/v1/chat/completions"
) {
    private val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun chat(userMessage: String, tools: List<Tool>? = null): String {
        val openRouterTools = tools?.map { mcpTool ->
            ToolDefinition(
                function = FunctionDefinition(
                    name = mcpTool.name,
                    description = mcpTool.description,
                    parameters = mcpSchemaToOpenAIParameters(mcpTool.inputSchema)
                ),
            )
        }
        val hasTools = !openRouterTools.isNullOrEmpty()
        val request = OpenRouterChatRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(
                OpenRouterMessage(role = "user", content = userMessage)
            ),
            tools = openRouterTools?.takeIf { it.isNotEmpty() },
        )

        val response: OpenRouterChatResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        response.error?.let { err ->
            val msg = "OpenRouter API error: ${err.message} (code: ${err.code})"
            if (err.code == "400" && hasTools) {
                throw IllegalStateException(
                    "$msg. Try without tools - the tool schema may be incompatible with the model."
                )
            }
            throw IllegalStateException(msg)
        }
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException(
                "OpenRouter response has no choices or content. " +
                    "Model may have returned tool_calls - use tool_choice or handle tool calls."
            )
        return content
    }

    fun close() {
        httpClient.close()
    }
}
