package org.example.mcp

import kotlinx.serialization.json.Json
import org.example.mcp.models.CallToolResult
import org.example.mcp.models.InitializeResult
import org.example.mcp.models.ListPromptsResult
import org.example.mcp.models.ListResourcesResult
import org.example.mcp.models.ReadResourceResult
import org.example.mcp.models.ListToolsResult
import java.io.File

/**
 * Common interface for MCP clients (HTTP and stdio transports)
 */
interface IMcpClient {
    suspend fun initialize(): InitializeResult
    suspend fun listResources(): ListResourcesResult
    suspend fun readResource(uri: String): ReadResourceResult
    suspend fun listTools(): ListToolsResult
    suspend fun callTool(name: String, arguments: kotlinx.serialization.json.JsonObject? = null): CallToolResult
    suspend fun listPrompts(): ListPromptsResult
    fun close()
}

/**
 * Factory for creating MCP clients from configuration.
 * Supports both HTTP (url) and stdio (command/args) transports.
 */
object McpClientFactory {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Create MCP client from configuration file
     * @param workspaceFolder Used for ${workspaceFolder} substitution in stdio configs (default: current dir)
     */
    fun fromConfigFile(configPath: String, serverName: String, workspaceFolder: String = System.getProperty("user.dir", ".")): IMcpClient {
        val configFile = File(configPath)
        if (!configFile.exists()) {
            throw IllegalArgumentException("Configuration file not found: $configPath")
        }

        val configText = configFile.readText()
        val serversConfig = json.decodeFromString<McpServersConfig>(configText)

        val serverConfig = serversConfig.allServers[serverName]
            ?: throw IllegalArgumentException("Server '$serverName' not found in configuration")

        return createClient(serverConfig, workspaceFolder)
    }

    /**
     * Create MCP client from configuration JSON string
     */
    fun fromConfigJson(configJson: String, serverName: String, workspaceFolder: String = System.getProperty("user.dir", ".")): IMcpClient {
        val serversConfig = json.decodeFromString<McpServersConfig>(configJson)

        val serverConfig = serversConfig.allServers[serverName]
            ?: throw IllegalArgumentException("Server '$serverName' not found in configuration")

        return createClient(serverConfig, workspaceFolder)
    }

    /**
     * Create MCP client directly from config
     */
    fun fromConfig(config: McpConfig, workspaceFolder: String = System.getProperty("user.dir", ".")): IMcpClient {
        return createClient(config, workspaceFolder)
    }

    private fun createClient(config: McpConfig, workspaceFolder: String): IMcpClient {
        return when {
            config.isStdio -> McpStdioClient(config, workspaceFolder)
            config.isHttp -> McpClient(config)
            else -> throw IllegalArgumentException("Config must have either 'url' (HTTP) or 'command' (stdio)")
        }
    }

    /**
     * List all available server names from configuration file
     */
    fun listServers(configPath: String): List<String> {
        val configFile = File(configPath)
        if (!configFile.exists()) {
            throw IllegalArgumentException("Configuration file not found: $configPath")
        }

        val configText = configFile.readText()
        val serversConfig = json.decodeFromString<McpServersConfig>(configText)

        return serversConfig.allServers.keys.toList()
    }
}
