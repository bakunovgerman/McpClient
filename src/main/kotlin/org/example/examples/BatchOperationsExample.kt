package org.example.examples

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.McpUtils

/**
 * Example of batch operations with MCP
 */
fun main() = runBlocking {
    println("=== Batch Operations MCP Client Example ===\n")

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
        println("Initializing...")
        client.initialize()
        println("✓ Connected\n")

        // Prepare batch of tool calls
        val libraries = listOf("react", "vue", "angular", "svelte")
        
        println("Resolving ${libraries.size} libraries in parallel...")
        
        val calls = libraries.map { library ->
            library to buildJsonObject {
                put("libraryName", library)
                put("query", "How to get started with $library?")
            }
        }

        // Execute all calls in parallel
        val results = McpUtils.callToolsInParallel(
            client,
            calls.map { (library, args) -> "resolve-library-id" to args }
        )

        // Process results
        println("\nResults:\n")
        libraries.zip(results).forEach { (library, result) ->
            println("Library: $library")
            result.fold(
                onSuccess = { toolResult ->
                    if (McpUtils.hasError(toolResult)) {
                        println("  ✗ Error occurred")
                    } else {
                        println("  ✓ Success")
                        val text = McpUtils.extractText(toolResult)
                        if (text.length > 100) {
                            println("  ${text.take(100)}...")
                        } else {
                            println("  $text")
                        }
                    }
                },
                onFailure = { error ->
                    println("  ✗ Failed: ${error.message}")
                }
            )
            println()
        }

        // Check tool availability
        println("--- Checking Tool Availability ---")
        val toolsToCheck = listOf("resolve-library-id", "query-docs", "non-existent-tool")
        
        toolsToCheck.forEach { toolName ->
            val exists = McpUtils.toolExists(client, toolName)
            val status = if (exists) "✓" else "✗"
            println("$status $toolName: ${if (exists) "Available" else "Not found"}")
        }

    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
        println("\nConnection closed.")
    }
}
