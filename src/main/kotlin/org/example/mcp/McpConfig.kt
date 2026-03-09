package org.example.mcp

import kotlinx.serialization.Serializable

/**
 * Configuration for MCP connection.
 * Supports two transports:
 * - HTTP: use `url` and optionally `headers`
 * - Stdio: use `command` and `args` (e.g. npx with -y and package name)
 */
@Serializable
data class McpConfig(
    val url: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val command: String? = null,
    val args: List<String> = emptyList(),
    val env: Map<String, String> = emptyMap()
) {
    val isStdio: Boolean get() = command != null
    val isHttp: Boolean get() = url != null
}

/**
 * MCP Servers configuration.
 * Supports both Cursor format ("servers") and legacy format ("mcpServers").
 */
@Serializable
data class McpServersConfig(
    val mcpServers: Map<String, McpConfig> = emptyMap(),
    val servers: Map<String, McpConfig> = emptyMap()
) {
    val allServers: Map<String, McpConfig>
        get() = if (mcpServers.isNotEmpty()) mcpServers else servers
}
