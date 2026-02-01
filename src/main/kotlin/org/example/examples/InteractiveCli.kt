package org.example.examples

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.mcp.McpClient
import org.example.mcp.McpConfig
import org.example.mcp.McpUtils

/**
 * Interactive CLI for MCP client
 */
fun main() = runBlocking {
    println("╔════════════════════════════════════════╗")
    println("║   Interactive MCP Client CLI           ║")
    println("╚════════════════════════════════════════╝\n")

    // Try to load API key from local.properties, fallback to user input
    val apiKey = McpUtils.loadProperty("local.properties", "CONTEXT7_API_KEY") ?: run {
        println("⚠ local.properties not found or CONTEXT7_API_KEY not set")
        print("Enter CONTEXT7_API_KEY: ")
        readLine()?.takeIf { it.isNotBlank() } ?: run {
            System.err.println("✗ API key is required")
            return@runBlocking
        }
    }

    val config = McpConfig(
        url = "https://mcp.context7.com/mcp",
        headers = mapOf("CONTEXT7_API_KEY" to apiKey)
    )

    val client = McpClient(config, "InteractiveCLI", "1.0.0",)

    try {
        // Initialize
        print("Connecting to MCP server... ")
        val initResult = client.initialize()
        println("✓")
        println("Server: ${initResult.serverInfo.name} v${initResult.serverInfo.version}\n")

        // Main loop
        var running = true
        while (running) {
            println("\n─────────────────────────────────────────")
            println("Commands:")
            println("  1 - List Tools")
            println("  2 - List Resources")
            println("  3 - List Prompts")
            println("  4 - Call Tool: resolve-library-id")
            println("  5 - Call Tool: query-docs")
            println("  6 - Show Server Info")
            println("  0 - Exit")
            println("─────────────────────────────────────────")
            print("Choose command: ")

            when (readLine()?.trim()) {
                "1" -> {
                    println("\n📋 Tools:")
                    val tools = client.listTools()
                    tools.tools.forEachIndexed { index, tool ->
                        println("\n${index + 1}. ${tool.name}")
                        tool.description?.let { println("   $it") }
                    }
                    println("\nTotal: ${tools.tools.size} tools")
                }

                "2" -> {
                    println("\n📦 Resources:")
                    try {
                        val resources = client.listResources()
                        resources.resources.forEachIndexed { index, resource ->
                            println("\n${index + 1}. ${resource.name}")
                            println("   URI: ${resource.uri}")
                            resource.description?.let { println("   $it") }
                        }
                        println("\nTotal: ${resources.resources.size} resources")
                    } catch (e: Exception) {
                        println("⚠ Resources not available: ${e.message}")
                    }
                }

                "3" -> {
                    println("\n💬 Prompts:")
                    try {
                        val prompts = client.listPrompts()
                        prompts.prompts.forEachIndexed { index, prompt ->
                            println("\n${index + 1}. ${prompt.name}")
                            prompt.description?.let { println("   $it") }
                            prompt.arguments?.forEach { arg ->
                                println("   - ${arg.name}${if (arg.required == true) " (required)" else ""}")
                            }
                        }
                        println("\nTotal: ${prompts.prompts.size} prompts")
                    } catch (e: Exception) {
                        println("⚠ Prompts not available: ${e.message}")
                    }
                }

                "4" -> {
                    println("\n🔧 Call: resolve-library-id")
                    print("Enter library name (e.g., react): ")
                    val libraryName = readLine()?.trim() ?: ""
                    
                    if (libraryName.isNotBlank()) {
                        print("Enter query (e.g., How to use hooks?): ")
                        val query = readLine()?.trim() ?: ""
                        
                        val args = buildJsonObject {
                            put("libraryName", libraryName)
                            put("query", query)
                        }
                        
                        try {
                            println("\nCalling tool...")
                            val result = client.callTool("resolve-library-id", args)
                            println("\n${McpUtils.prettyPrintToolResult(result)}")
                        } catch (e: Exception) {
                            println("✗ Error: ${e.message}")
                        }
                    } else {
                        println("✗ Library name cannot be empty")
                    }
                }

                "5" -> {
                    println("\n🔧 Call: query-docs")
                    print("Enter library ID (e.g., /facebook/react): ")
                    val libraryId = readLine()?.trim() ?: ""
                    
                    if (libraryId.isNotBlank()) {
                        print("Enter query: ")
                        val query = readLine()?.trim() ?: ""
                        
                        val args = buildJsonObject {
                            put("libraryId", libraryId)
                            put("query", query)
                        }
                        
                        try {
                            println("\nCalling tool...")
                            val result = client.callTool("query-docs", args)
                            println("\n${McpUtils.prettyPrintToolResult(result)}")
                        } catch (e: Exception) {
                            println("✗ Error: ${e.message}")
                        }
                    } else {
                        println("✗ Library ID cannot be empty")
                    }
                }

                "6" -> {
                    println("\n📊 Server Information:")
                    println("Name: ${initResult.serverInfo.name}")
                    println("Version: ${initResult.serverInfo.version}")
                    println("Protocol: ${initResult.protocolVersion}")
                    println("\nCapabilities:")
                    initResult.capabilities.tools?.let {
                        println("  ✓ Tools")
                    }
                    initResult.capabilities.resources?.let {
                        println("  ✓ Resources")
                    }
                    initResult.capabilities.prompts?.let {
                        println("  ✓ Prompts")
                    }
                }

                "0" -> {
                    println("\nExiting...")
                    running = false
                }

                else -> {
                    println("✗ Invalid command")
                }
            }
        }

    } catch (e: Exception) {
        System.err.println("\n✗ Fatal Error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
        println("\n✓ Connection closed. Goodbye!")
    }
}
