package org.example.mcp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.example.mcp.models.*
import java.util.UUID

/**
 * MCP Client for connecting to remote MCP servers
 */
class McpClient(
    private val config: McpConfig,
    private val clientName: String = "KotlinMcpClient",
    private val clientVersion: String = "1.0.0",
) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    // JSON for encoding requests - always include jsonrpc
    private val jsonEncoder = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true  // Always encode jsonrpc field
        explicitNulls = false  // Don't include explicit null fields
    }
    
    // JSON for decoding responses
    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    private var isInitialized = false
    private val initMutex = Mutex()
    private var serverInfo: ServerInfo? = null
    private var serverCapabilities: ServerCapabilities? = null

    /**
     * Initialize the MCP connection
     */
    suspend fun initialize(): InitializeResult {
        initMutex.withLock {
            if (isInitialized) {
                return InitializeResult(
                    protocolVersion = "2024-11-05",
                    capabilities = serverCapabilities!!,
                    serverInfo = serverInfo!!
                )
            }

            val params = InitializeParams(
                protocolVersion = "2024-11-05",
                capabilities = ClientCapabilities(),
                clientInfo = ClientInfo(
                    name = clientName,
                    version = clientVersion
                )
            )

            val result = sendRequest<InitializeResult>(
                method = "initialize",
                params = jsonEncoder.encodeToJsonElement(params).jsonObject
            )

            serverInfo = result.serverInfo
            serverCapabilities = result.capabilities
            isInitialized = true

            println("✓ Connected to MCP server: ${result.serverInfo.name} v${result.serverInfo.version}")
            println("✓ Protocol version: ${result.protocolVersion}")

            return result
        }
    }

    /**
     * List available resources
     */
    suspend fun listResources(): ListResourcesResult {
        ensureInitialized()
        return sendRequest("resources/list")
    }

    /**
     * Read a specific resource
     */
    suspend fun readResource(uri: String): ReadResourceResult {
        ensureInitialized()
        val params = ReadResourceParams(uri = uri)
        return sendRequest(
            method = "resources/read",
            params = jsonEncoder.encodeToJsonElement(params).jsonObject
        )
    }

    /**
     * List available tools
     */
    suspend fun listTools(): ListToolsResult {
        ensureInitialized()
        return sendRequest("tools/list")
    }

    /**
     * Call a tool
     */
    suspend fun callTool(name: String, arguments: JsonObject? = null): CallToolResult {
        ensureInitialized()
        val params = CallToolParams(name = name, arguments = arguments)
        return sendRequest(
            method = "tools/call",
            params = jsonEncoder.encodeToJsonElement(params).jsonObject
        )
    }

    /**
     * List available prompts
     */
    suspend fun listPrompts(): ListPromptsResult {
        ensureInitialized()
        return sendRequest("prompts/list")
    }

    /**
     * Send a generic JSON-RPC request
     */
    private suspend inline fun <reified T> sendRequest(
        method: String,
        params: JsonObject? = null
    ): T {
        val requestId = UUID.randomUUID().toString()
        val request = JsonRpcRequest(
            id = requestId,
            method = method,
            params = params
        )
        
        val requestBody = jsonEncoder.encodeToString(request)
        
        // Always show request for debugging Parse error
        println("\n=== REQUEST ===")
        println("URL: ${config.url}")
        println("Body: $requestBody")
        println("===============\n")

        val response: HttpResponse = httpClient.post(config.url) {
            contentType(ContentType.Application.Json)
            
            // MCP requires both application/json and text/event-stream in Accept header
            header("Accept", "application/json, text/event-stream")
            
            // Add custom headers from config
            config.headers.forEach { (key, value) ->
                header(key, value)
            }
            
            setBody(requestBody)
        }

        val responseText = response.bodyAsText()
        
        // Always show response for debugging Parse error
        println("=== RESPONSE ===")
        println("Status: ${response.status}")
        println("Content-Type: ${response.contentType()}")
        println("Body: $responseText")
        println("================\n")
        
        // Try to parse JSON response
        val jsonRpcResponse = try {
            json.decodeFromString<JsonRpcResponse>(responseText)
        } catch (e: Exception) {
            throw McpException(
                "Failed to parse response as JSON: ${e.message}. Response was: ${responseText.take(200)}",
                JsonRpcError(-32700, "Parse error", null)
            )
        }

        if (jsonRpcResponse.error != null) {
            throw McpException(
                "MCP Error [${jsonRpcResponse.error.code}]: ${jsonRpcResponse.error.message}",
                jsonRpcResponse.error
            )
        }

        if (jsonRpcResponse.result == null) {
            throw McpException("MCP Response has no result")
        }

        return json.decodeFromJsonElement(jsonRpcResponse.result)
    }

    /**
     * Ensure the client is initialized
     */
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            initialize()
        }
    }

    /**
     * Close the client and release resources
     */
    fun close() {
        httpClient.close()
    }
}

/**
 * MCP Exception
 */
class McpException(
    message: String,
    val error: JsonRpcError? = null
) : Exception(message)
