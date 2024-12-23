package jetbrains.xodus.browser.web.db

import io.ktor.server.plugins.NotFoundException
import jetbrains.xodus.browser.web.DBSummary

interface DatabasesStore {
    fun start()
    fun add(summary: DBSummary): DBSummary
    fun update(uuid: String, summary: DBSummary): DBSummary
    fun delete(uuid: String)
    fun all(): List<DBSummary>
    fun stop()
    fun find(uuid: String, error: () -> Nothing = {
        throw NotFoundException("Database not found by id '$uuid'")
    }): DBSummary
}