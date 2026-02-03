package org.example.weather

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

private val logger = LoggerFactory.getLogger("WeatherServer")

fun main() {
    logger.info("=== Запуск Weather AI Server ===")
    
    // Инициализация БД
    DatabaseFactory.init()
    logger.info("База данных инициализирована")
    
    // Инициализация OpenRouter клиента
    val apiKey = System.getenv("OPENROUTER_API_KEY") 
        ?: throw IllegalStateException("OPENROUTER_API_KEY environment variable is not set")
    val openRouterClient = OpenRouterClient(apiKey)
    logger.info("OpenRouter клиент инициализирован")
    
    // Инициализация Weather Agent
    val weatherAgent = WeatherAgent(openRouterClient)
    logger.info("Weather Agent инициализирован")
    
    // Запуск сервера
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureServer()
        configureRouting(weatherAgent)
        
        // Запуск периодического запроса погоды
        launch {
            startWeatherScheduler(weatherAgent)
        }
    }.start(wait = true)
}

fun Application.configureServer() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureRouting(weatherAgent: WeatherAgent) {
    routing {
        get("/") {
            call.respondText("Weather AI Server is running!")
        }
        
        get("/health") {
            call.respondText("OK")
        }
        
        get("/weather/latest") {
            val latest = weatherAgent.getLatestWeatherRecords(10)
            call.respond(latest)
        }
        
        get("/weather/all") {
            val all = weatherAgent.getAllWeatherRecords()
            call.respond(all)
        }
        
        get("/weather/check-now") {
            logger.info("Ручной запрос погоды через API")
            weatherAgent.checkWeather()
            call.respondText("Weather check initiated")
        }
    }
}

suspend fun startWeatherScheduler(weatherAgent: WeatherAgent) {
    logger.info("=== Запуск планировщика погоды (каждую минуту) ===")
    
    // Первый запрос сразу
    weatherAgent.checkWeather()
    
    // Затем каждую минуту
    while (true) {
        delay(1.minutes)
        weatherAgent.checkWeather()
    }
}
