package org.example.examples

import kotlinx.coroutines.runBlocking
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.McpUtils

/**
 * Basic example of using MCP client
 */
fun main() = runBlocking {
    println("=== Basic MCP Client Example ===\n")

    // Load API key from local.properties
    val apiKey = try {
        McpUtils.requireProperty("local.properties", "CONTEXT7_API_KEY")
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        return@runBlocking
    }

    // Create configuration
    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf(
            "CONTEXT7_API_KEY" to apiKey
        )
    )

    // Create and use client
    val client = McpClient(
        config = config,
        clientName = "BasicExample",
        clientVersion = "1.0.0",
    )

    try {
        // Initialize
        println("Initializing connection...")
        val initResult = client.initialize()
        println("✓ Connected to: ${initResult.serverInfo.name} v${initResult.serverInfo.version}")
        println("✓ Protocol: ${initResult.protocolVersion}\n")

        // List tools
        println("Available Tools:")
        val tools = client.listTools()
        tools.tools.forEachIndexed { index, tool ->
            println("${index + 1}. ${tool.name}")
            if (tool.description != null) {
                println("   ${tool.description}")
            }
        }
        println()

    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
        println("Connection closed.")
    }
}
