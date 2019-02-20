package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.DBSummary
import java.util.concurrent.ConcurrentHashMap

open class InMemoryDatabasesStore : DatabasesStore {

    val databases: MutableMap<String, DBSummary> = ConcurrentHashMap()

    override fun start() {
    }

    override fun add(dbSummary: DBSummary): DBSummary {
        val copy = dbSummary.copy()
        databases[dbSummary.uuid] = copy
        return copy
    }

    override fun update(uuid: String, summary: DBSummary): DBSummary {
        val copy = summary.copy()
        databases[uuid] = copy
        return copy
    }

    override fun delete(uuid: String) {
        databases.remove(uuid)
    }

    override fun all(): List<DBSummary> {
        return databases.values.map { it.copy() }
    }

    override fun stop() {
    }

    override fun find(uuid: String, error: () -> Nothing): DBSummary {
        return databases[uuid] ?: error()
    }
}