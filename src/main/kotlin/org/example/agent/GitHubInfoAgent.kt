package org.example.agent

import kotlinx.coroutines.runBlocking
import org.example.mcp.IMcpClient
import org.example.mcp.McpClientFactory
import org.example.mcp.McpUtils
import org.example.mcp.models.Tool
import org.example.openrouter.OpenRouterClient
import java.io.File

private const val CONFIG_PATH = "src/main/resources/mcp-config.json"
private val PROMPT = """
    Мне нужно узнать сколько веток в GitHub репозитории https://github.com/bakunovgerman/McpClient.
    После получения информации сохрани результат в файл github-info с помощью инструмента write_file.
""".trimIndent()

private fun loadApiKey(): String {
    val envFile = File(".env")
    if (envFile.exists()) {
        val props = McpUtils.loadProperties(".env")
        props.getProperty("OPENROUTER_API_KEY")?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return System.getenv("OPENROUTER_API_KEY") ?: ""
}

fun main() = runBlocking {
    val apiKey = loadApiKey()
    if (apiKey.isNullOrBlank()) {
        System.err.println("OPENROUTER_API_KEY is not set. Set it and run again.")
        kotlin.system.exitProcess(1)
    }

    val workspaceFolder = System.getProperty("user.dir", ".")
    val serverNames = McpClientFactory.listServers(CONFIG_PATH)

    val clients = mutableMapOf<String, IMcpClient>()
    val toolToClient = mutableMapOf<String, IMcpClient>()
    val allTools = mutableListOf<Tool>()

    try {
        for (serverName in serverNames) {
            val client = McpClientFactory.fromConfigFile(CONFIG_PATH, serverName, workspaceFolder)
            client.initialize()
            clients[serverName] = client

            val toolsResult = client.listTools()
            for (tool in toolsResult.tools) {
                toolToClient[tool.name] = client
                allTools.add(tool)
            }
        }

        val openRouterClient = OpenRouterClient(apiKey)

        val toolExecutor: org.example.openrouter.ToolExecutor = { name, args ->
            val client = toolToClient[name]
                ?: throw IllegalStateException("Unknown tool: $name")
            client.callTool(name, args)
        }

        println("Sending prompt to LLM with ${allTools.size} tools from ${clients.size} MCP servers...")
        val response = openRouterClient.chat(
            userMessage = PROMPT,
            tools = allTools,
            toolExecutor = toolExecutor
        )

        println("LLM response: $response")
    } finally {
        clients.values.forEach { it.close() }
    }
}
