package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.DBSummary

class PersistentDatabaseService : DatabaseService {

    private val storedDatabases = StoredDatabases()

    override val isReadonly: Boolean
        get() = false

    override fun all(): List<DBSummary> {
        return storedDatabases.all()
    }

    override fun start() {
        storedDatabases.start()
    }

    override fun find(uuid: String): DBSummary? {
        return storedDatabases.find(uuid)
    }

    override fun add(dbSummary: DBSummary): DBSummary {
        val newSummary = storedDatabases.add(dbSummary)
        if (dbSummary.isOpened) {
            return markStarted(newSummary.uuid, false)
        }
        return newSummary
    }

    override fun markStarted(uuid: String, started: Boolean): DBSummary {
        val summary = storedDatabases.find(uuid)
        summary.isOpened = started
        return storedDatabases.update(uuid, summary)
    }

    override fun delete(uuid: String) {
        return storedDatabases.delete(uuid)
    }

    override fun deleteAll() {
        storedDatabases.all().forEach { delete(it.uuid) }
    }


    override fun stop() {
        storedDatabases.stop()
    }
}