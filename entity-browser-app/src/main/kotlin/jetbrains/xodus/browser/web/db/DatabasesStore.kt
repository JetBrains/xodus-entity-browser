package jetbrains.xodus.browser.web.db

//import io.ktor.features.NotFoundException
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.NotFoundException

interface DatabasesStore {
    fun start()
    fun add(dbSummary: DBSummary): DBSummary
    fun update(uuid: String, summary: DBSummary): DBSummary
    fun delete(uuid: String)
    fun all(): List<DBSummary>
    fun stop()
    fun find(uuid: String, error: () -> Nothing = {
        throw NotFoundException("Database not found by id '$uuid'")
    }): DBSummary
}