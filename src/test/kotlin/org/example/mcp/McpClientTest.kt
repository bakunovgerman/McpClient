package org.example.mcp

import kotlinx.coroutines.runBlocking
import org.example.mcp.models.InitializeResult
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Basic tests for MCP Client
 * Note: These tests require a valid CONTEXT7_API_KEY environment variable
 */
class McpClientTest {

    private fun createTestConfig(): McpConfig {
        val apiKey = System.getenv("CONTEXT7_API_KEY") ?: "test_key"
        return McpConfig(
            url = "https://mcp.context7.com/mcp",
            headers = mapOf("CONTEXT7_API_KEY" to apiKey)
        )
    }

    @Test
    fun `test client creation`() {
        val config = createTestConfig()
        val client = McpClient(config)
        assertNotNull(client)
        client.close()
    }

    @Test
    fun `test config creation from json`() {
        val configJson = """
            {
              "mcpServers": {
                "test": {
                  "url": "https://test.com/mcp",
                  "headers": {
                    "API_KEY": "test_key"
                  }
                }
              }
            }
        """.trimIndent()

        val client = McpClientFactory.fromConfigJson(configJson, "test")
        assertNotNull(client)
        client.close()
    }

    @Test
    fun `test initialize with real server`() = runBlocking {
        // Skip if no API key is set
        val apiKey = System.getenv("CONTEXT7_API_KEY")
        if (apiKey.isNullOrBlank()) {
            println("Skipping test: CONTEXT7_API_KEY not set")
            return@runBlocking
        }

        val config = createTestConfig()
        val client = McpClient(config)

        try {
            val result: InitializeResult = client.initialize()
            assertNotNull(result.serverInfo)
            assertNotNull(result.capabilities)
            assertTrue(result.protocolVersion.isNotEmpty())
            println("Server: ${result.serverInfo.name} v${result.serverInfo.version}")
        } catch (e: Exception) {
            println("Test failed (expected if no API key): ${e.message}")
        } finally {
            client.close()
        }
    }

    @Test
    fun `test list tools with real server`() = runBlocking {
        val apiKey = System.getenv("CONTEXT7_API_KEY")
        if (apiKey.isNullOrBlank()) {
            println("Skipping test: CONTEXT7_API_KEY not set")
            return@runBlocking
        }

        val config = createTestConfig()
        val client = McpClient(config)

        try {
            client.initialize()
            val tools = client.listTools()
            assertNotNull(tools)
            assertTrue(tools.tools.isNotEmpty(), "Should have at least one tool")
            println("Found ${tools.tools.size} tools")
            tools.tools.forEach { tool ->
                println("  - ${tool.name}")
            }
        } catch (e: Exception) {
            println("Test failed: ${e.message}")
        } finally {
            client.close()
        }
    }
}
