package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.Application
import jetbrains.xodus.browser.web.DBSummary


class DatabaseService {

    fun add(dbSummary: DBSummary): DBSummary {
        val newSummary = Databases.add(dbSummary)
        if (dbSummary.isOpened) {
            return tryStart(newSummary.uuid, false)
        }
        return newSummary
    }

    fun tryStart(uuid: String, silent: Boolean = true): DBSummary {
        val summary = Databases.find(uuid)
        summary.isOpened = Application.tryStartServices(summary, silent)
        return Databases.update(uuid, summary)
    }

    fun stop(uuid: String): DBSummary {
        val summary = Databases.find(uuid)
        Application.stop(summary)
        summary.isOpened = false
        return Databases.update(uuid, summary)
    }

    fun delete(uuid: String) {
        val summary = Databases.find(uuid)
        Application.stop(summary)
        return Databases.delete(uuid)
    }

    fun deleteAll() {
        Databases.all().forEach { delete(it.uuid) }
    }

}