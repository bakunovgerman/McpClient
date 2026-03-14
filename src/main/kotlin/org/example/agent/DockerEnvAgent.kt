package org.example.agent

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.example.mcp.IMcpClient
import org.example.mcp.McpClientFactory
import org.example.mcp.McpUtils
import org.example.mcp.models.CallToolResult
import org.example.mcp.models.Tool
import org.example.openrouter.OpenRouterClient
import org.example.openrouter.ToolExecutor
import java.io.File

/** Разрешённые ключевые слова в команде для run_process. Если команда содержит хотя бы одно — выполняется без подтверждения. */
private val ALLOWED_COMMAND_KEYWORDS = setOf("docker", "podman")

/** Извлекает строку команды из аргументов run_process. Поддерживает command, command_line, argv. */
private fun extractCommandFromArgs(args: kotlinx.serialization.json.JsonObject?): String {
    if (args == null) return ""
    for (key in listOf("command", "command_line", "argv")) {
        val value = args[key] ?: continue
        val str = when (value) {
            is JsonPrimitive -> value.content.toString()
            is JsonArray -> value.joinToString(" ") { (it as? JsonPrimitive)?.content?.toString() ?: it.toString() }
            else -> value.toString()
        }
        if (str.isNotBlank()) {
            return str
        }
    }
    return args.toString()
}

/** Создаёт обёртку над toolExecutor с проверкой разрешённых команд для run_process. */
private fun wrapToolExecutorWithCommandCheck(
    baseExecutor: ToolExecutor,
    allowedKeywords: Set<String> = ALLOWED_COMMAND_KEYWORDS
): ToolExecutor = { name, args ->
    if (name != "run_process") {
        baseExecutor(name, args)
    } else {
        val command = extractCommandFromArgs(args)
        val isAllowed = allowedKeywords.any { keyword ->
            keyword.lowercase() in command.lowercase()
        }
        if (!isAllowed) {
            print("Команда не в разрешённом списке (${allowedKeywords.joinToString()}): $command\nВыполнить? [y/N]: ")
            val answer = readlnOrNull()?.trim()?.lowercase()
            if (answer != "y" && answer != "yes") {
                CallToolResult(
                    content = listOf(
                        org.example.mcp.models.ToolContent(text = "Пользователь отклонил выполнение команды: $command")
                    ),
                    isError = true
                )
            } else {
                baseExecutor(name, args)
            }
        } else {
            baseExecutor(name, args)
        }
    }
}

private fun configPath(): String {
    System.getenv("MCP_CONFIG_PATH")?.let { path ->
        if (java.io.File(path).exists()) return path
    }
    return sequenceOf(
        "resources/mcp-config.json",           // Docker: /app/resources
        "src/main/resources/mcp-config.json"   // Local dev
    ).firstOrNull { java.io.File(it).exists() } ?: "src/main/resources/mcp-config.json"
}

/** Тестовый промпт для проверки версии Java (команда java в разрешённом списке). */
private val TEST_JAVA_PROMPT = """
    Проверь версию Java в системе
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

    val userPrompt = args.firstOrNull()?.takeIf { it.isNotBlank() } ?: TEST_JAVA_PROMPT
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
        val baseExecutor: ToolExecutor = { name, args ->
            val client = toolToClient[name]
                ?: throw IllegalStateException("Unknown tool: $name")
            client.callTool(name, args)
        }
        val toolExecutor = wrapToolExecutorWithCommandCheck(baseExecutor)

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
