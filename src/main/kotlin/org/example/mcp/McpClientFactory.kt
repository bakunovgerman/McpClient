package org.example.mcp

import kotlinx.serialization.json.Json
import java.io.File

/**
 * Factory for creating MCP clients from configuration
 */
object McpClientFactory {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Create MCP client from configuration file
     */
    fun fromConfigFile(configPath: String, serverName: String): McpClient {
        val configFile = File(configPath)
        if (!configFile.exists()) {
            throw IllegalArgumentException("Configuration file not found: $configPath")
        }

        val configText = configFile.readText()
        val serversConfig = json.decodeFromString<McpServersConfig>(configText)

        val serverConfig = serversConfig.mcpServers[serverName]
            ?: throw IllegalArgumentException("Server '$serverName' not found in configuration")

        return McpClient(serverConfig)
    }

    /**
     * Create MCP client from configuration JSON string
     */
    fun fromConfigJson(configJson: String, serverName: String): McpClient {
        val serversConfig = json.decodeFromString<McpServersConfig>(configJson)
        
        val serverConfig = serversConfig.mcpServers[serverName]
            ?: throw IllegalArgumentException("Server '$serverName' not found in configuration")

        return McpClient(serverConfig)
    }

    /**
     * Create MCP client directly from config
     */
    fun fromConfig(config: McpConfig): McpClient {
        return McpClient(config)
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

        return serversConfig.mcpServers.keys.toList()
    }
}
