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
import org.example.mcp.McpUtils
import org.example.mcp.models.CallToolResult
import org.example.mcp.models.Tool

/** Executes a tool by name with JSON arguments. Returns result or null if tool not found. */
typealias ToolExecutor = suspend (String, JsonObject?) -> CallToolResult

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

    /**
     * Sends a chat message to OpenRouter. When [toolExecutor] is provided and the LLM returns
     * tool_calls, those tools are executed and the conversation continues until a final answer.
     *
     * @param userMessage The user's message
     * @param tools Optional list of MCP tools to offer to the model
     * @param toolExecutor When provided, executes tools requested by the LLM (e.g. via McpClient.callTool)
     * @param maxToolRounds Maximum number of tool-call rounds (default 20) to prevent infinite loops
     */
    suspend fun chat(
        userMessage: String,
        tools: List<Tool>? = null,
        toolExecutor: ToolExecutor? = null,
        maxToolRounds: Int = 20
    ): String {
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
        val canExecuteTools = toolExecutor != null && hasTools

        var messages = mutableListOf(
            OpenRouterMessage(role = "user", content = userMessage)
        )

        var round = 0
        while (round < maxToolRounds) {
            val request = OpenRouterChatRequest(
                model = "openai/gpt-4o-mini",
                messages = messages,
                tools = openRouterTools?.takeIf { it.isNotEmpty() },
                tool_choice = if (hasTools) "auto" else null,
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

            val choice = response.choices.firstOrNull()
                ?: throw IllegalStateException("OpenRouter response has no choices")
            val message = choice.message
            val finishReason = choice.finish_reason ?: "stop"

            when {
                finishReason == "stop" && message.content != null -> {
                    return message.content
                }
                finishReason == "tool_calls" && !message.tool_calls.isNullOrEmpty() && canExecuteTools -> {
                    messages = messages.toMutableList()
                    messages.add(
                        OpenRouterMessage(
                            role = "assistant",
                            content = message.content,
                            tool_calls = message.tool_calls
                        )
                    )
                    for (tc in message.tool_calls) {
                        val args = parseToolArguments(tc.function.arguments)
                        val result = toolExecutor!!(tc.function.name, args)
                        val resultText = McpUtils.extractText(result)
                        val errorPrefix = if (result.isError == true) "Error: " else ""
                        messages.add(
                            OpenRouterMessage(
                                role = "tool",
                                content = errorPrefix + resultText.ifEmpty { "{}" },
                                tool_call_id = tc.id,
                                name = tc.function.name
                            )
                        )
                    }
                    round++
                }
                else -> {
                    throw IllegalStateException(
                        "OpenRouter returned finish_reason=$finishReason with no content. " +
                            "Model requested tool_calls but toolExecutor was not provided."
                    )
                }
            }
        }
        throw IllegalStateException("Exceeded max tool rounds ($maxToolRounds)")
    }

    private fun parseToolArguments(arguments: String): JsonObject? {
        return when {
            arguments.isBlank() -> buildJsonObject { }
            else -> try {
                Json.decodeFromString<JsonObject>(arguments)
            } catch (_: Exception) {
                buildJsonObject { put("raw", JsonPrimitive(arguments)) }
            }
        }
    }

    fun close() {
        httpClient.close()
    }
}
