package org.example.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.ZoneOffset

private val logger = LoggerFactory.getLogger("DatabaseFactory")

object DatabaseFactory {
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL") ?: "jdbc:h2:./data/weather_db;AUTO_SERVER=TRUE"
        val driverClassName = if (databaseUrl.startsWith("jdbc:postgresql")) {
            "org.postgresql.Driver"
        } else {
            "org.h2.Driver"
        }
        
        logger.info("=== Инициализация БД ===")
        logger.info("URL: $databaseUrl")
        logger.info("Driver: $driverClassName")
        
        val database = Database.connect(
            url = databaseUrl,
            driver = driverClassName
        )
        
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(WeatherRecords)
            logger.info("Таблица WeatherRecords создана или уже существует")
        }
    }
    
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { 
            addLogger(StdOutSqlLogger)
            block() 
        }
}

object WeatherRecords : Table("weather_records") {
    val id = integer("id").autoIncrement()
    val timestamp = timestamp("timestamp")
    val weatherResponse = text("weather_response")
    val modelUsed = varchar("model_used", 100)
    
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class WeatherRecord(
    val id: Int,
    val timestamp: String,
    val weatherResponse: String,
    val modelUsed: String
)

suspend fun insertWeatherRecord(weatherResponse: String, modelUsed: String): Int {
    logger.info("=== Сохранение записи в БД ===")
    logger.info("Модель: $modelUsed")
    logger.info("Длина ответа: ${weatherResponse.length} символов")
    
    return DatabaseFactory.dbQuery {
        val id = WeatherRecords.insert {
            it[timestamp] = java.time.Instant.now()
            it[WeatherRecords.weatherResponse] = weatherResponse
            it[WeatherRecords.modelUsed] = modelUsed
        }[WeatherRecords.id]
        
        logger.info("Запись сохранена с ID: $id")
        id
    }
}

suspend fun getLatestWeatherRecords(limit: Int = 10): List<WeatherRecord> {
    logger.info("=== Получение последних $limit записей из БД ===")
    
    return DatabaseFactory.dbQuery {
        WeatherRecords
            .selectAll()
            .orderBy(WeatherRecords.timestamp to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                WeatherRecord(
                    id = row[WeatherRecords.id],
                    timestamp = row[WeatherRecords.timestamp].atOffset(ZoneOffset.UTC).toString(),
                    weatherResponse = row[WeatherRecords.weatherResponse],
                    modelUsed = row[WeatherRecords.modelUsed]
                )
            }.also {
                logger.info("Получено ${it.size} записей")
            }
    }
}

suspend fun getAllWeatherRecords(): List<WeatherRecord> {
    logger.info("=== Получение всех записей из БД ===")
    
    return DatabaseFactory.dbQuery {
        WeatherRecords
            .selectAll()
            .orderBy(WeatherRecords.timestamp to SortOrder.DESC)
            .map { row ->
                WeatherRecord(
                    id = row[WeatherRecords.id],
                    timestamp = row[WeatherRecords.timestamp].atOffset(ZoneOffset.UTC).toString(),
                    weatherResponse = row[WeatherRecords.weatherResponse],
                    modelUsed = row[WeatherRecords.modelUsed]
                )
            }.also {
                logger.info("Получено ${it.size} записей")
            }
    }
}
