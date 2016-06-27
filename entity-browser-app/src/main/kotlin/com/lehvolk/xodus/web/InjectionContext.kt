package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.JobsService
import com.lehvolk.xodus.web.db.StoreService
import com.lehvolk.xodus.web.db.dbFilter


class Context(val storeService: StoreService,
              val jobsService: JobsService = JobsService()) {

    fun destroy() {
        jobsService.shutdown()
        storeService.destroy()
    }

}

object InjectionContexts {

    val allContexts = hashMapOf<String, Context>()

    val current: Context get() {
        return allContexts[Databases.current()!!.uuid]!!
    }

    fun start() {
        Databases.allOpened().forEach {
            val context = Context(StoreService(XodusStoreRequisites(it.location!!, it.key!!)))
            allContexts.put(it.uuid, context)
        }
        val requisites = XodusStore.lookupRequisites()
        if (requisites != null) {
            val db = DB().apply {
                location = requisites.location
                key = requisites.key
            }
            val opened = Databases.allOpened().find(dbFilter(db))
            if (opened == null) {
                Databases.open(db)
                allContexts.put(db.uuid, Context(StoreService(db.asRequisites())))
            }
        }
    }

    fun destroy() {
        allContexts.forEach { it.value.destroy() }
    }

    fun start(db: DB) {
        val opened = Databases.allOpened().find(dbFilter(db))
        if (opened == null) {
            Databases.add(db)
            Databases.open(db)
            allContexts.put(db.uuid, Context(StoreService(db.asRequisites())))
        }
    }

    fun stop(db: DB) {
        allContexts[db.uuid]?.destroy()
    }

}
