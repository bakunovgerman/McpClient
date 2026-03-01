package org.example.openrouter

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.storage.LlmResponseDb
import org.example.storage.LlmResponseRecord
import java.time.Duration
import java.time.Instant

private const val MCP_SERVER_URL = "https://fittable-deeanna-noneditorially.ngrok-free.dev/mcp"
private const val POLL_INTERVAL_MS = 20_000L
private const val SUMMARY_INTERVAL_SECONDS = 60L

fun main() {
    val apiKey = System.getenv("OPENROUTER_API_KEY")
    if (apiKey.isNullOrBlank()) {
        System.err.println("OPENROUTER_API_KEY is not set. Set it and run again.")
        kotlin.system.exitProcess(1)
    }

    val openRouterClient = OpenRouterClient(apiKey)
    val mcpClient = McpClient(
        config = McpConfig(
            url = MCP_SERVER_URL,
            headers = mapOf("ngrok-skip-browser-warning" to "true")
        )
    )
    val db = LlmResponseDb()
    Runtime.getRuntime().addShutdownHook(Thread {
        openRouterClient.close()
        mcpClient.close()
    })

    runBlocking {
        val tools = mcpClient.listTools().tools
        val userMessage = "мне нужно узнать сколько веток в GitHub репозитории https://github.com/bakunovgerman/McpClient"

        launch {
            delay(SUMMARY_INTERVAL_SECONDS * 1000)
            while (true) {
                try {
                    val now = Instant.now()
                    val from = now.minus(Duration.ofMinutes(1))
                    val records = db.getByTimeRange(from, now)
                    if (records.isEmpty()) {
                        println("[${now}] Summary: no new records in the last minute")
                    } else {
                        val summary = getSummaryFromLlm(openRouterClient, records)
                        showMacNotification("LLM Summary", summary)
                        println("Summary: - $summary")
                      //  println("[${now}] Summary: notified ${records.size} record(s)")
                    }
                } catch (e: Exception) {
                    System.err.println("[${Instant.now()}] Summary error: ${e.message}")
                    e.printStackTrace(System.err)
                }
            }
        }

        while (true) {
            try {
                val response = openRouterClient.chat(
                    userMessage = userMessage,
                    tools = tools,
                    toolExecutor = { name, args -> mcpClient.callTool(name, args) }
                )
                db.save(userMessage, response)
                val ts = Instant.now()
                println("[$ts] Tools: ${tools.size}, Response: $response")
            } catch (e: Exception) {
                System.err.println("[${Instant.now()}] Error: ${e.message}")
                e.printStackTrace(System.err)
            }
            delay(POLL_INTERVAL_MS)
        }
    }
}

private suspend fun getSummaryFromLlm(client: OpenRouterClient, records: List<LlmResponseRecord>): String {
    val dataText = records.joinToString("\n---\n") { record ->
        buildString {
            append("Time: ${record.createdAt}\n")
            record.userMessage?.let { append("User: $it\n") }
            append("Response: ${record.response}")
        }
    }
    val prompt = """
        Сделай краткое резюме (summary) следующей информации из базы данных.
        Ответь на русском языке, максимум 2-3 предложения.
        
        Данные:
        $dataText
    """.trimIndent()
    return client.chat(userMessage = prompt)
}

private fun showMacNotification(title: String, message: String) {
    val escapedTitle = title.replace("'", "\\'").replace("\"", "\\\"")
    val escapedMessage = message
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", " ")
        .take(200)
    val script = "display notification \"$escapedMessage\" with title \"$escapedTitle\""
    try {
        ProcessBuilder("osascript", "-e", script)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
            .waitFor()
    } catch (e: Exception) {
        System.err.println("Failed to show notification: ${e.message}")
    }
}
