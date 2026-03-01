package org.example.storage

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant

/**
 * SQLite storage for LLM responses.
 * Saves date (timestamp), user message and final answer from the LLM.
 *
 * Database file: `llm_responses.db` in the project root (or current working directory).
 *
 * --- Как просматривать таблицы БД ---
 *
 * 1. SQLite CLI (встроен в macOS):
 *    sqlite3 llm_responses.db
 *    .tables                    # список таблиц
 *    SELECT * FROM llm_responses;
 *    .mode column               # красивый вывод
 *    .headers on
 *    .quit
 *
 * 2. DB Browser for SQLite (GUI, бесплатно):
 *    https://sqlitebrowser.org/
 *    Открыть файл llm_responses.db
 *
 * 3. VS Code / Cursor: расширение "SQLite Viewer" или "SQLite"
 *
 * 4. Из терминала одной командой:
 *    sqlite3 llm_responses.db "SELECT id, created_at, substr(response, 1, 80) FROM llm_responses;"
 */
class LlmResponseDb(dbPath: String = "llm_responses.db") {
    private val url = "jdbc:sqlite:${File(dbPath).absolutePath}"

    init {
        Class.forName("org.sqlite.JDBC")
        withConnection { conn ->
            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS llm_responses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at TEXT NOT NULL,
                    user_message TEXT,
                    response TEXT NOT NULL
                )
            """.trimIndent())
        }
    }

    fun save(userMessage: String?, response: String) {
        val now = Instant.now().toString()
        withConnection { conn ->
            conn.prepareStatement(
                "INSERT INTO llm_responses (created_at, user_message, response) VALUES (?, ?, ?)"
            ).use { stmt ->
                stmt.setString(1, now)
                stmt.setString(2, userMessage)
                stmt.setString(3, response)
                stmt.executeUpdate()
            }
        }
    }

    private fun <T> withConnection(block: (Connection) -> T): T {
        DriverManager.getConnection(url).use { conn ->
            return block(conn)
        }
    }

    companion object {
        /** Default path to DB file (project root). */
        fun defaultPath(): String = "llm_responses.db"
    }
}
