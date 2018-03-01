package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.DatabaseService
import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.JobsService
import com.lehvolk.xodus.web.db.StoreService


class Services(val storeService: StoreService,
               val jobsService: JobsService = JobsService()) {

    fun stop() {
        jobsService.stop()
        storeService.stop()
    }

}

object Application {

    internal val allServices = hashMapOf<String, Services>()

    private val databaseService: DatabaseService = DatabaseService()

    fun start() {
        Databases.all().forEach { databaseService.tryStart(it.uuid) }
    }

    fun tryStartServices(db: DBSummary): Boolean {
        if (allServices.containsKey(db.uuid)) {
            return true
        }
        val service = try {
            StoreService(db)
        } catch (e: Exception) {
            null
        }
        service?.let {
            allServices[db.uuid] = Services(it)
        }
        return service != null
    }

    fun stop(db: DBSummary) {
        allServices[db.uuid]?.also {
            it.stop()
        }
        allServices.remove(db.uuid)
    }

}


fun servicesOf(dbUUID: String): Services = Application.allServices[dbUUID]!!

infix fun String.systemOr(default: String): String = System.getProperty(this, default)

fun String.system(): String = System.getProperty(this)



