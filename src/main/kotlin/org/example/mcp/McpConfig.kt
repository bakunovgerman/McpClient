package org.example.mcp

import kotlinx.serialization.Serializable

/**
 * Configuration for MCP connection
 */
@Serializable
data class McpConfig(
    val url: String,
    val headers: Map<String, String> = emptyMap()
)

/**
 * MCP Servers configuration
 */
@Serializable
data class McpServersConfig(
    val mcpServers: Map<String, McpConfig>
)
