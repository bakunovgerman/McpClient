package org.example

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig

fun main() = runBlocking {
    println("=== MCP Client Example ===\n")

    val apiKey = try {
        org.example.mcp.McpUtils.requireProperty("local.properties", "CONTEXT7_API_KEY")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        return@runBlocking
    }

    val config = McpConfig(
        url = "https://fittable-deeanna-noneditorially.ngrok-free.dev/mcp",
        headers = mapOf()
    )

    val client = McpClient(config)

    try {
        println("Connecting to MCP server...")
        val initResult = client.initialize()
        println("Server capabilities: ${initResult.capabilities}\n")

        println("--- Listing Tools ---")
        val tools = client.listTools()
        tools.tools.forEach { tool ->
            println("Tool: ${tool.name}")
            println("  Description: ${tool.description ?: "N/A"}")
            println("  Input Schema: ${tool.inputSchema}")
            println()
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        // Clean up
        client.close()
        println("\nConnection closed.")
    }
}