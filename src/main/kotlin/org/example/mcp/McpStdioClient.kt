package org.example.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.example.mcp.models.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * MCP Client for stdio transport (e.g. npx @modelcontextprotocol/server-filesystem).
 * Spawns the server as subprocess and communicates via stdin/stdout with newline-delimited JSON-RPC.
 */
@OptIn(ExperimentalSerializationApi::class)
class McpStdioClient(
    private val config: McpConfig,
    private val workspaceFolder: String = System.getProperty("user.dir", "."),
    private val clientName: String = "KotlinMcpClient",
    private val clientVersion: String = "1.0.0",
) : IMcpClient {
    init {
        require(config.command != null) { "McpStdioClient requires command for stdio transport" }
    }

    private val jsonEncoder = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    private var process: Process? = null
    private var writer: OutputStreamWriter? = null
    private var reader: BufferedReader? = null
    private val pendingResponses = ConcurrentHashMap<String, Channel<JsonRpcResponse>>()
    private val writeMutex = Mutex()

    private var isInitialized = false
    private val initMutex = Mutex()
    private var serverInfo: ServerInfo? = null
    private var serverCapabilities: ServerCapabilities? = null

    private fun substituteArgs(args: List<String>): List<String> {
        return args.map { arg ->
            arg.replace("\${workspaceFolder}", workspaceFolder)
                .replace("\${workspace_folder}", workspaceFolder)
        }
    }

    private fun startProcess() {
        val command = config.command!!
        val args = substituteArgs(config.args)
        val fullCommand = listOf(command) + args

        val processBuilder = ProcessBuilder(fullCommand)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)

        val env = processBuilder.environment()
        config.env.forEach { (k, v) -> env[k] = v }

        process = processBuilder.start()
        writer = OutputStreamWriter(process!!.outputStream, Charsets.UTF_8)
        reader = BufferedReader(InputStreamReader(process!!.inputStream, Charsets.UTF_8))

        thread(name = "mcp-stdio-reader", isDaemon = true) {
            runBlocking { readLoop() }
        }
    }

    private suspend fun readLoop() {
        val r = reader ?: return
        try {
            while (true) {
                val line = withContext(Dispatchers.IO) { r.readLine() } ?: break
                if (line.isBlank()) continue
                try {
                    val response = json.decodeFromString<JsonRpcResponse>(line)
                    response.id?.let { id ->
                        pendingResponses[id]?.trySend(response)  // Channel(1) buffers until receive()
                    }
                } catch (_: Exception) {
                    // Skip invalid JSON (e.g. server logs that went to stdout by mistake)
                }
            }
        } catch (_: Exception) {
            // Process closed
        }
    }

    private suspend fun sendRequest(method: String, params: JsonObject? = null): JsonElement {
        val requestId = UUID.randomUUID().toString()
        val channel = Channel<JsonRpcResponse>(1)
        pendingResponses[requestId] = channel

        val request = JsonRpcRequest(
            id = requestId,
            method = method,
            params = params
        )
        val requestBody = jsonEncoder.encodeToString(request)

        writeMutex.withLock {
            writer?.let {
                withContext(Dispatchers.IO) {
                    it.write(requestBody)
                    it.write("\n")
                    it.flush()
                }
            } ?: throw McpException("Process not started or already closed")
        }

        val response = channel.receive()
        pendingResponses.remove(requestId)

        if (response.error != null) {
            throw McpException(
                "MCP Error [${response.error.code}]: ${response.error.message}",
                response.error
            )
        }

        return response.result ?: throw McpException("MCP Response has no result")
    }

    private suspend fun sendNotification(method: String, params: JsonObject? = null) {
        val request = JsonRpcNotification(method = method, params = params)
        val requestBody = jsonEncoder.encodeToString(request)
        writeMutex.withLock {
            writer?.let {
                withContext(Dispatchers.IO) {
                    it.write(requestBody)
                    it.write("\n")
                    it.flush()
                }
            }
        }
    }

    private suspend inline fun <reified T> sendRequestTyped(method: String, params: JsonObject? = null): T {
        val result = sendRequest(method, params)
        return json.decodeFromJsonElement(result)
    }

    override suspend fun initialize(): InitializeResult {
        initMutex.withLock {
            if (isInitialized) {
                return InitializeResult(
                    protocolVersion = "2024-11-05",
                    capabilities = serverCapabilities!!,
                    serverInfo = serverInfo!!
                )
            }

            if (process == null) {
                startProcess()
            }

            val params = InitializeParams(
                protocolVersion = "2024-11-05",
                capabilities = ClientCapabilities(),
                clientInfo = ClientInfo(name = clientName, version = clientVersion)
            )

            val result = sendRequestTyped<InitializeResult>(
                method = "initialize",
                params = jsonEncoder.encodeToJsonElement(params).jsonObject
            )

            serverInfo = result.serverInfo
            serverCapabilities = result.capabilities

            // Send initialized notification (required by MCP lifecycle)
            sendNotification("notifications/initialized", buildJsonObject { })

            isInitialized = true

            println("✓ Connected to MCP server: ${result.serverInfo.name} v${result.serverInfo.version}")
            println("✓ Protocol version: ${result.protocolVersion}")

            return result
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) initialize()
    }

    override suspend fun listResources(): ListResourcesResult {
        ensureInitialized()
        return sendRequestTyped("resources/list")
    }

    override suspend fun readResource(uri: String): ReadResourceResult {
        ensureInitialized()
        val params = ReadResourceParams(uri = uri)
        return sendRequestTyped("resources/read", jsonEncoder.encodeToJsonElement(params).jsonObject)
    }

    override suspend fun listTools(): ListToolsResult {
        ensureInitialized()
        return sendRequestTyped("tools/list")
    }

    override suspend fun callTool(name: String, arguments: JsonObject?): CallToolResult {
        ensureInitialized()
        val params = CallToolParams(name = name, arguments = arguments)
        return sendRequestTyped("tools/call", jsonEncoder.encodeToJsonElement(params).jsonObject)
    }

    override suspend fun listPrompts(): ListPromptsResult {
        ensureInitialized()
        return sendRequestTyped("prompts/list")
    }

    override fun close() {
        process?.destroy()
        process = null
        writer = null
        reader = null
    }
}
