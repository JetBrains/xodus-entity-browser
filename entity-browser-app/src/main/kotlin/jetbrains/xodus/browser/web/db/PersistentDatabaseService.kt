package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.DBSummary

open class PersistentDatabaseService(private val store: DatabasesStore = JsonDatabasesStore()) : DatabaseService {

    override val isReadonly: Boolean
        get() = false

    override fun all(): List<DBSummary> {
        return store.all()
    }

    override fun find(uuid: String): DBSummary? {
        return store.find(uuid)
    }

    override fun add(dbSummary: DBSummary): DBSummary {
        val newSummary = store.add(dbSummary)
        if (dbSummary.isOpened) {
            return markStarted(newSummary.uuid, false)
        }
        return newSummary
    }

    override fun markStarted(uuid: String, started: Boolean): DBSummary {
        val summary = store.find(uuid)
        summary.isOpened = started
        return store.update(uuid, summary)
    }

    override fun delete(uuid: String) {
        return store.delete(uuid)
    }

    override fun deleteAll() {
        store.all().forEach { delete(it.uuid) }
    }
}