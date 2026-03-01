package org.example.openrouter

import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterMessage>
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
    val choices: List<OpenRouterChoice>,
    val usage: OpenRouterUsage? = null
)

@Serializable
data class OpenRouterUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)
