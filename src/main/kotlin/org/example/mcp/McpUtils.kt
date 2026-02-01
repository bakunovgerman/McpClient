package org.example.mcp

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import org.example.mcp.models.CallToolResult
import org.example.mcp.models.Tool
import java.io.File
import java.util.Properties

/**
 * Utility functions for working with MCP
 */
object McpUtils {
    /**
     * Load properties from a file
     */
    fun loadProperties(filename: String): Properties {
        val properties = Properties()
        val file = File(filename)
        
        if (!file.exists()) {
            throw IllegalStateException(
                "Properties file not found: $filename\n" +
                "Please create '$filename' file with required properties.\n" +
                "See '${filename}.example' for reference."
            )
        }
        
        file.inputStream().use { properties.load(it) }
        return properties
    }
    
    /**
     * Load property value or return null
     */
    fun loadProperty(filename: String, key: String): String? {
        return try {
            loadProperties(filename).getProperty(key)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Load property value or throw exception
     */
    fun requireProperty(filename: String, key: String): String {
        return loadProperty(filename, key)
            ?: throw IllegalStateException("Required property '$key' not found in $filename")
    }
    /**
     * Find a tool by name
     */
    suspend fun findTool(client: McpClient, toolName: String): Tool? {
        val tools = client.listTools()
        return tools.tools.find { it.name == toolName }
    }

    /**
     * Check if a tool exists
     */
    suspend fun toolExists(client: McpClient, toolName: String): Boolean {
        return findTool(client, toolName) != null
    }

    /**
     * Call multiple tools in parallel
     */
    suspend fun callToolsInParallel(
        client: McpClient,
        calls: List<Pair<String, JsonObject?>>
    ): List<Result<CallToolResult>> = coroutineScope {
        calls.map { (toolName, arguments) ->
            async {
                runCatching {
                    client.callTool(toolName, arguments)
                }
            }
        }.awaitAll()
    }

    /**
     * Pretty print a tool
     */
    fun prettyPrintTool(tool: Tool): String {
        return buildString {
            appendLine("Tool: ${tool.name}")
            tool.description?.let {
                appendLine("Description: $it")
            }
            appendLine("Input Schema:")
            appendLine(tool.inputSchema.toString().prependIndent("  "))
        }
    }

    /**
     * Pretty print tool result
     */
    fun prettyPrintToolResult(result: CallToolResult): String {
        return buildString {
            appendLine("Result (Error: ${result.isError ?: false}):")
            result.content.forEachIndexed { index, content ->
                appendLine("Content #${index + 1}:")
                appendLine("  Type: ${content.type}")
                content.text?.let {
                    appendLine("  Text: ${if (it.length > 200) it.take(200) + "..." else it}")
                }
                content.mimeType?.let {
                    appendLine("  MIME Type: $it")
                }
            }
        }
    }

    /**
     * Extract text from tool result
     */
    fun extractText(result: CallToolResult): String {
        return result.content
            .mapNotNull { it.text }
            .joinToString("\n\n")
    }

    /**
     * Check if result has error
     */
    fun hasError(result: CallToolResult): Boolean {
        return result.isError == true
    }
}
