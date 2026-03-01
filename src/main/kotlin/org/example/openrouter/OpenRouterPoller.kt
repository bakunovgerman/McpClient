package org.example.openrouter

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import java.time.Instant

private const val MCP_SERVER_URL = "https://fittable-deeanna-noneditorially.ngrok-free.dev/mcp"
private const val POLL_INTERVAL_MS = 20_000L

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
    Runtime.getRuntime().addShutdownHook(Thread {
        openRouterClient.close()
        mcpClient.close()
    })

    runBlocking {
        val tools = mcpClient.listTools().tools
        while (true) {
            try {
                val response = openRouterClient.chat(
                    userMessage = "мне нужно узнать сколько веток в GitHub репозитории https://github.com/bakunovgerman/McpClient",
                    tools = tools,
                    toolExecutor = { name, args -> mcpClient.callTool(name, args) }
                )
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
