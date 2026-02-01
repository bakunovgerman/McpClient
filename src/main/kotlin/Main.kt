package org.example

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.McpException

fun main(args: Array<String>) {
    runBlocking {
        println("=== MCP Client Example with Tool Call Test ===\n")

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
        println("Found ${tools.tools.size} available tools:\n")
        
        tools.tools.forEach { tool ->
            println("Tool: ${tool.name}")
            println("  Description: ${tool.description ?: "N/A"}")
            println("  Input Schema: ${tool.inputSchema}")
            println()
        }

        // Test callTool method
        if (tools.tools.isNotEmpty()) {
            val firstTool = tools.tools.first()
            println("\n--- Testing callTool Method ---")
            println("Attempting to call tool: ${firstTool.name}\n")

            try {
                // Build arguments based on the first tool's schema
                val arguments = when (firstTool.name) {
                    "list_branches", "get_default_branch" -> buildJsonObject {
                        put("owner", "bakunovgerman")
                        put("repo", "McpClient")
                    }
                    else -> buildJsonObject {
                        put("test", "value")
                    }
                }

                val result = client.callTool(firstTool.name, arguments)
                
                println("✓ Tool call successful!")
                println("Is Error: ${result.isError}")
                println("\nTool Response:")
                result.content.forEach { content ->
                    println("  Content Type: ${content.type}")
                    content.text?.let { text ->
                        val preview = if (text.length > 300) {
                            text.substring(0, 300) + "...\n  [truncated]"
                        } else {
                            text
                        }
                        println("  Text:\n    $preview")
                    }
                    println()
                }
            } catch (e: McpException) {
                println("✗ Tool call failed: ${e.message}")
                e.error?.let { error ->
                    println("  Error Code: ${error.code}")
                    println("  Error Message: ${error.message}")
                }
            } catch (e: Exception) {
                println("✗ Unexpected error: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("No tools available to test callTool method")
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
}