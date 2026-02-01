package org.example.examples

import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClientFactory

/**
 * Example of using MCP client with configuration file
 */
fun main() = runBlocking {
    println("=== Config File MCP Client Example ===\n")

    val configPath = "src/main/resources/mcp-config.json"
    val serverName = "context7"

    try {
        // List available servers
        println("Available servers in config:")
        val servers = McpClientFactory.listServers(configPath)
        servers.forEachIndexed { index, name ->
            println("${index + 1}. $name")
        }
        println()

        // Create client from config file
        println("Connecting to server: $serverName")
        val client = McpClientFactory.fromConfigFile(configPath, serverName)

        // Initialize
        val initResult = client.initialize()
        println("✓ Connected to: ${initResult.serverInfo.name}")
        println()

        // List tools
        println("Available Tools:")
        val tools = client.listTools()
        tools.tools.forEach { tool ->
            println("• ${tool.name}")
        }

        // Clean up
        client.close()

    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    }
}
