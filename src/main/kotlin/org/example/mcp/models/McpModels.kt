package org.example.mcp.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * MCP Initialize Request Parameters
 */
@Serializable
data class InitializeParams(
    val protocolVersion: String = "2024-11-05",
    val capabilities: ClientCapabilities = ClientCapabilities(),
    val clientInfo: ClientInfo
)

@Serializable
class ClientCapabilities {
    // Empty capabilities object - server doesn't accept null values
    // This will serialize as {}
}

@Serializable
data class RootsCapability(
    val listChanged: Boolean = false
)

@Serializable
data class ClientInfo(
    val name: String,
    val version: String
)

/**
 * MCP Initialize Response
 */
@Serializable
data class InitializeResult(
    val protocolVersion: String,
    val capabilities: ServerCapabilities,
    val serverInfo: ServerInfo
)

@Serializable
data class ServerCapabilities(
    val logging: JsonObject? = null,
    val prompts: PromptsCapability? = null,
    val resources: ResourcesCapability? = null,
    val tools: ToolsCapability? = null
)

@Serializable
data class PromptsCapability(
    val listChanged: Boolean? = null
)

@Serializable
data class ResourcesCapability(
    val subscribe: Boolean? = null,
    val listChanged: Boolean? = null
)

@Serializable
data class ToolsCapability(
    val listChanged: Boolean? = null
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String
)

/**
 * MCP Resource
 */
@Serializable
data class Resource(
    val uri: String,
    val name: String,
    val description: String? = null,
    val mimeType: String? = null
)

/**
 * MCP Tool
 */
@Serializable
data class Tool(
    val name: String,
    val description: String? = null,
    val inputSchema: JsonObject
)

/**
 * MCP Prompt
 */
@Serializable
data class Prompt(
    val name: String,
    val description: String? = null,
    val arguments: List<PromptArgument>? = null
)

@Serializable
data class PromptArgument(
    val name: String,
    val description: String? = null,
    val required: Boolean? = null
)

/**
 * List Resources Response
 */
@Serializable
data class ListResourcesResult(
    val resources: List<Resource>,
    val nextCursor: String? = null
)

/**
 * List Tools Response
 */
@Serializable
data class ListToolsResult(
    val tools: List<Tool>,
    val nextCursor: String? = null
)

/**
 * List Prompts Response
 */
@Serializable
data class ListPromptsResult(
    val prompts: List<Prompt>,
    val nextCursor: String? = null
)

/**
 * Read Resource Request
 */
@Serializable
data class ReadResourceParams(
    val uri: String
)

/**
 * Read Resource Response
 */
@Serializable
data class ReadResourceResult(
    val contents: List<ResourceContents>
)

@Serializable
data class ResourceContents(
    val uri: String,
    val mimeType: String? = null,
    val text: String? = null,
    val blob: String? = null
)

/**
 * Call Tool Request
 */
@Serializable
data class CallToolParams(
    val name: String,
    val arguments: JsonObject? = null
)

/**
 * Call Tool Response
 */
@Serializable
data class CallToolResult(
    val content: List<ToolContent>,
    val isError: Boolean? = null
)

@Serializable
data class ToolContent(
    val type: String,
    val text: String? = null,
    val data: String? = null,
    val mimeType: String? = null
)
