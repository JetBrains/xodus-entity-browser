package com.lehvolk.xodus.web.db

import com.lehvolk.xodus.web.Application
import com.lehvolk.xodus.web.DBSummary


class DatabaseService {

    fun add(location: String, key: String, isOpened: Boolean): DBSummary {
        val newDB = Databases.add(location, key)
        return if (isOpened) {
            tryStart(newDB.uuid)
        } else {
            newDB
        }
    }

    fun tryStart(uuid: String): DBSummary {
        return Databases.applyChange(uuid) {
            val servicesStarted = Application.tryStartServices(this)
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