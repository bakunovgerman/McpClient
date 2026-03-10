package org.example.agent

import kotlinx.coroutines.runBlocking
import org.example.mcp.IMcpClient
import org.example.mcp.McpClientFactory
import org.example.mcp.McpUtils
import org.example.mcp.models.Tool
import org.example.openrouter.OpenRouterClient
import java.io.File

private fun configPath(): String {
    System.getenv("MCP_CONFIG_PATH")?.let { path ->
        if (java.io.File(path).exists()) return path
    }
    return sequenceOf(
        "resources/mcp-config.json",           // Docker: /app/resources
        "src/main/resources/mcp-config.json"   // Local dev
    ).firstOrNull { java.io.File(it).exists() } ?: "src/main/resources/mcp-config.json"
}

private val DEFAULT_PROMPT = """
    Подключись к реальному окружению и выполни следующие шаги:
    1. Проверь, что Docker доступен (выполни `docker --version` или `docker ps`)
    2. Запусти тестовый контейнер: `docker run --rm hello-world`
    3. Сохрани полный вывод команд в файл docker-output с помощью инструмента write_file

    Если Docker недоступен, попробуй альтернативу (например, podman) или запиши в файл информацию об ошибке.
""".trimIndent()

private fun loadApiKey(): String {
    val envFile = File(".env")
    if (envFile.exists()) {
        val props = McpUtils.loadProperties(".env")
        props.getProperty("OPENROUTER_API_KEY")?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return System.getenv("OPENROUTER_API_KEY") ?: ""
}

fun main(args: Array<String>) = runBlocking {
    val apiKey = loadApiKey()
    if (apiKey.isBlank()) {
        System.err.println("OPENROUTER_API_KEY is not set. Set it in .env or environment and run again.")
        kotlin.system.exitProcess(1)
    }

    val userPrompt = args.firstOrNull()?.takeIf { it.isNotBlank() } ?: DEFAULT_PROMPT
    val workspaceFolder = System.getenv("WORKSPACE_FOLDER") ?: System.getProperty("user.dir", ".")

    val configPath = configPath()
    val serverNames = McpClientFactory.listServers(configPath)
    val clients = mutableMapOf<String, IMcpClient>()
    val toolToClient = mutableMapOf<String, IMcpClient>()
    val allTools = mutableListOf<Tool>()

    try {
        for (serverName in serverNames) {
            val client = McpClientFactory.fromConfigFile(configPath, serverName, workspaceFolder)
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

        println("DockerEnvAgent: ${allTools.size} tools from ${clients.size} MCP servers")
        println("Tools: ${allTools.map { it.name }.joinToString(", ")}")
        println("---")
        println("Prompt: $userPrompt")
        println("---")

        val response = openRouterClient.chat(
            userMessage = userPrompt,
            tools = allTools,
            toolExecutor = toolExecutor
        )

        println("Result: $response")
    } finally {
        clients.values.forEach { it.close() }
    }
}
