package org.example.openrouter

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String? = null,
    val tool_calls: List<OpenRouterToolCall>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OpenRouterToolCall(
    val id: String,
    @EncodeDefault
    val type: String = "function",
    val function: OpenRouterToolCallFunction
)

@Serializable
data class OpenRouterToolCallFunction(
    val name: String,
    val arguments: String
)

@Serializable
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Double = 1.0,
    val tools: List<ToolDefinition>? = null,
    val tool_choice: String? = null,  // "auto" | "none" | {"type": "function", "function": {"name": "..."}}
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ToolDefinition(
    @EncodeDefault
    val type: String = "function",
    val function: FunctionDefinition
)

@Serializable
data class FunctionDefinition(
    val name: String,
    val description: String? = null,
    val parameters: JsonObject? = null
)

@Serializable
data class OpenRouterChoice(
    val index: Int? = null,
    val message: OpenRouterMessage,
    val finish_reason: String? = null
)

@Serializable
data class OpenRouterChatResponse(
    val id: String? = null,
    val choices: List<OpenRouterChoice> = emptyList(),
    val usage: OpenRouterUsage? = null,
    val error: OpenRouterError? = null
)

@Serializable
data class OpenRouterError(
    val message: String,
    val code: String? = null,
    val type: String? = null
)

@Serializable
data class OpenRouterUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)
