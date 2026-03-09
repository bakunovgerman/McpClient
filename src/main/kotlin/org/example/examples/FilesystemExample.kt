package org.example.examples

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClientFactory

/**
 * Example of using MCP client with npx filesystem server
 */
fun main() = runBlocking {
    println("=== Filesystem MCP Client Example (npx) ===\n")

    val configPath = "src/main/resources/mcp-config.json"
    val serverName = "filesystem"
    val workspaceFolder = System.getProperty("user.dir", ".")

    try {
        println("Connecting to server: $serverName (workspace: $workspaceFolder)")
        val client = McpClientFactory.fromConfigFile(configPath, serverName, workspaceFolder)

        client.initialize()
        println("✓ Connected\n")

        val tools = client.listTools()
        println("Available Tools:")
        tools.tools.forEach { println("• ${it.name}") }

        // Call read_file or read_text_file if available
        val readTool = tools.tools.find { it.name == "read_file" || it.name == "read_text_file" }
        if (readTool != null) {
            println("\nCalling ${readTool.name} on build.gradle.kts...")
            val result = client.callTool(
                readTool.name,
                buildJsonObject { put("path", "build.gradle.kts") }
            )
            result.content.forEach { c ->
                c.text?.let { println(it.take(300) + if (it.length > 300) "..." else "") }
            }
        }

        client.close()
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    }
}
