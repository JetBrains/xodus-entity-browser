package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.JobsService
import com.lehvolk.xodus.web.db.StoreService
import com.lehvolk.xodus.web.db.dbFilter
import javax.ws.rs.NotFoundException


class Context(val storeService: StoreService,
              val jobsService: JobsService = JobsService()) {

    fun destroy() {
        jobsService.shutdown()
        storeService.destroy()
    }

}

object InjectionContexts {

    private val allContexts = hashMapOf<String, Context>()

    fun start() {
        Databases.allRecent().forEach {
            val service = try {
                StoreService(XodusStoreRequisites(it.location!!, it.key!!))
            } catch (e: Exception) {
                null
            }
            if (service != null) {
                it.isOpened = true
                val context = Context(service)
                allContexts.put(it.uuid, context)
            } else {
                Databases.onStartupFail(it)
            }
        }
    }

    fun destroy() {
        allContexts.forEach { it.value.destroy() }
    }

    fun start(db: DBSummary) {
        val opened = Databases.allRecent().find(dbFilter(db))
        if (opened == null) {
            Databases.add(db)
            Databases.open(db)
            allContexts.put(db.uuid, Context(StoreService(db.asRequisites())))
        }
    }

    fun stop(db: DBSummary) {
        allContexts[db.uuid]?.destroy()
    }

    fun of(db: DBSummary): Context = allContexts[db.uuid] ?: throw NotFoundException()

}
