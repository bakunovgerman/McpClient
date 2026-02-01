package org.example.examples

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.McpException
import org.example.mcp.McpUtils

/**
 * Example of calling MCP tools
 */
fun main() = runBlocking {
    println("=== Tool Call MCP Client Example ===\n")

    // Load API key from local.properties
    val apiKey = try {
        McpUtils.requireProperty("local.properties", "CONTEXT7_API_KEY")
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        return@runBlocking
    }

    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf(
            "CONTEXT7_API_KEY" to apiKey
        )
    )

    val client = McpClient(config)

    try {
        // Initialize
        client.initialize()

        // List available tools
        val tools = client.listTools()
        println("Found ${tools.tools.size} tools\n")

        // Example: Call resolve-library-id tool
        println("--- Calling 'resolve-library-id' tool ---")
        val resolveArgs = buildJsonObject {
            put("libraryName", "react")
            put("query", "How to use React hooks?")
        }

        try {
            val result = client.callTool("resolve-library-id", resolveArgs)
            println("Success: ${result.isError == false}")
            result.content.forEach { content ->
                println("\nContent Type: ${content.type}")
                content.text?.let { println("Text:\n$it") }
            }
        } catch (e: McpException) {
            println("Tool call failed: ${e.message}")
            e.error?.let { error ->
                println("Error Code: ${error.code}")
                println("Error Message: ${error.message}")
            }
        }

        println()

        // Example: Call query-docs tool
        println("--- Calling 'query-docs' tool ---")
        val queryArgs = buildJsonObject {
            put("libraryId", "/facebook/react")
            put("query", "How to use useState hook?")
        }

        try {
            val result = client.callTool("query-docs", queryArgs)
            println("Success: ${result.isError == false}")
            result.content.forEach { content ->
                println("\nContent Type: ${content.type}")
                content.text?.let { 
                    // Limit output for readability
                    val preview = if (it.length > 500) {
                        it.substring(0, 500) + "..."
                    } else {
                        it
                    }
                    println("Text:\n$preview")
                }
            }
        } catch (e: McpException) {
            println("Tool call failed: ${e.message}")
        }

    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
        println("\nConnection closed.")
    }
}
