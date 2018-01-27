package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.JobsService
import com.lehvolk.xodus.web.db.StoreService
import javax.ws.rs.NotFoundException


class Services(val storeService: StoreService,
               val jobsService: JobsService = JobsService()) {

    fun stop() {
        jobsService.stop()
        storeService.stop()
    }

}

object Application {

    internal val allServices = hashMapOf<String, Services>()

    fun start() {
        Databases.all().forEach { tryStart(it) }
    }

    fun tryStart(db: DBSummary): DBSummary {
        val service = try {
            val location = db.location ?: throw IllegalStateException("location can't be null")
            val key = db.key ?: throw IllegalStateException("key can't be null")
            StoreService(location, key)
        } catch (e: Exception) {
            null
        }
        if (service != null) {
            db.isOpened = true
            allServices[db.uuid] = Services(service)
        } else {
            Databases.markUnavailable(db.uuid)
        }
        return db
    }

    fun stop() {
        allServices.forEach { it.value.stop() }
    }

    fun stop(db: DBSummary) {
        allServices[db.uuid]?.also {
            it.stop()
        }
        allServices.remove(db.uuid)
    }

}


fun servicesOf(db: DBSummary): Services = Application.allServices[db.uuid] ?: throw NotFoundException()

infix fun String.systemOr(default: String): String = System.getProperty(this, default)
fun String.system(): String = System.getProperty(this)



