package org.example.openrouter

import io.ktor.client.plugins.logging.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** Logger that pretty-prints JSON in log messages for readability. */
val prettyJsonLogger: Logger = object : Logger {
    private val delegate = Logger.DEFAULT
    private val prettyJson = Json { prettyPrint = true; ignoreUnknownKeys = true; isLenient = true }

    override fun log(message: String) {
        delegate.log(tryPrettyPrint(message))
    }

    private fun tryPrettyPrint(message: String): String {
        val (jsonPart, startIdx, endIdx) = extractJsonPart(message) ?: return message
        return try {
            val element = Json.parseToJsonElement(jsonPart)
            val pretty = prettyJson.encodeToString(JsonElement.serializer(), element)
            message.substring(0, startIdx) + pretty + message.substring(endIdx)
        } catch (_: Exception) {
            message
        }
    }

    /** Returns (jsonString, startIndex, endIndex) or null. Handles Ktor "BODY START" / "BODY END" format. */
    private fun extractJsonPart(message: String): Triple<String, Int, Int>? {
        val bodyStartMarker = "BODY START"
        val searchFrom = message.indexOf(bodyStartMarker).takeIf { it >= 0 }?.let {
            message.indexOf("\n", it) + 1
        } ?: 0
        if (searchFrom <= 0) {
            val trimmed = message.trim()
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                val start = message.indexOf(trimmed)
                return Triple(trimmed, start, start + trimmed.length)
            }
            return null
        }
        val openBracket = message.indexOf("{", searchFrom).takeIf { it >= 0 }
            ?: message.indexOf("[", searchFrom).takeIf { it >= 0 } ?: return null
        val (openCh, closeCh) = if (message[openBracket] == '{') '{' to '}' else '[' to ']'
        var depth = 1
        var i = openBracket + 1
        while (i < message.length && depth > 0) {
            when {
                message[i] == '"' -> {
                    i++
                    while (i < message.length) {
                        if (message[i] == '\\') i += 2
                        else if (message[i] == '"') break
                        else i++
                    }
                    i++
                }
                message[i] == openCh -> { depth++; i++ }
                message[i] == closeCh -> { depth--; if (depth == 0) break; i++ }
                else -> i++
            }
        }
        if (depth != 0) return null
        val jsonPart = message.substring(openBracket, i + 1)
        return Triple(jsonPart, openBracket, i + 1)
    }
}
