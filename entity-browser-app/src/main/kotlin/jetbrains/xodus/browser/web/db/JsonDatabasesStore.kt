package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.Home
import jetbrains.xodus.browser.web.NotFoundException
import mu.KLogging


class JsonDatabasesStore : DatabasesStore {

    companion object : KLogging()

    private val loader by lazy { JsonDatabasesLoader(location) }

    private val location: String
        get() = System.getProperty("xodus.entity.browser.db.store") ?: Home.dbHome.absolutePath

    override fun start() {
    }

    override fun add(summary: DBSummary): DBSummary {
        if (!loader.add(summary)) {
            throw NotFoundException("Database on '${summary.location}' is already registered")
        }
        return summary
    }

    override fun update(uuid: String, summary: DBSummary): DBSummary {
        if (!loader.update(summary)) {
            throw NotFoundException("Database on '$location' can't be modified")
        }
        return summary
    }

    override fun delete(uuid: String) {
        if (!loader.delete(uuid)) {
            throw NotFoundException("Can not find database by id '$uuid'")
        }
    }

    override fun all() = listDBs()

    override fun stop() {
    }

    override fun find(uuid: String, error: () -> Nothing) = loader.get(uuid) ?: error()

    private fun listDBs() = loader.getAll()
}


