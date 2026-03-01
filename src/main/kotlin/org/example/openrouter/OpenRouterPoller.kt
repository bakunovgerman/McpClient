package org.example.openrouter

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Instant

fun main() {
    val apiKey = System.getenv("OPENROUTER_API_KEY")
    if (apiKey.isNullOrBlank()) {
        System.err.println("OPENROUTER_API_KEY is not set. Set it and run again.")
        kotlin.system.exitProcess(1)
    }

    val client = OpenRouterClient(apiKey)
    Runtime.getRuntime().addShutdownHook(Thread { client.close() })

    runBlocking {
        while (true) {
            delay(60_000)
            try {
                val response = client.chat("как дела?")
                val ts = Instant.now()
                println("[$ts] $response")
            } catch (e: Exception) {
                System.err.println("[${Instant.now()}] Error: ${e.message}")
                e.printStackTrace(System.err)
            }
        }
    }
}
