package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.Application
import jetbrains.xodus.browser.web.DBSummary


class DatabaseService {

    fun add(dbSummary: DBSummary): DBSummary {
        return Databases.add(dbSummary) {
            if (dbSummary.isOpened) {
                tryStart(uuid, false)
            }
        }
    }

    fun tryStart(uuid: String, silent: Boolean = true): DBSummary {
        return Databases.applyChange(uuid) {
            val servicesStarted = Application.tryStartServices(this, silent)
            isOpened = servicesStarted
        }
    }

    fun stop(uuid: String): DBSummary {
        return Databases.applyChange(uuid) {
            Application.stop(this)
            isOpened = false
        }
    }

    fun delete(uuid: String) {
        Databases.applyChange(uuid) {
            Application.stop(this)
        }
        return Databases.delete(uuid)
    }

    fun deleteAll() {
        Databases.all().forEach { delete(it.uuid) }
    }

}