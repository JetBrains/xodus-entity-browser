package com.lehvolk.xodus.web.db

import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.InjectionContexts
import com.lehvolk.xodus.web.JacksonConfigurator
import java.io.File
import java.util.*

private val mapper = JacksonConfigurator().mapper

fun dbFilter(db: DBSummary): (DBSummary) -> Boolean {
    return { db.location == it.location && db.key == it.key }
}

object Databases {

    private val dbs = arrayListOf<DBSummary>()

    private val recentStore = DBStore("./recent-dbs.json", dbs).load()

    fun add(db: DBSummary) {
        synchronized(this) {
            db.uuid = UUID.randomUUID().toString()
            dbs.add(db)
            recentStore.doSync()
        }
    }

    fun delete(db: DBSummary) {
        val predicate = dbFilter(db)
        synchronized(this) {
            dbs.removeAll(predicate)
            if (db.isOpened) {
                InjectionContexts.stop(db)
            }
            recentStore.doSync()
        }
    }

    fun open(db: DBSummary) {
        synchronized(this) {
            db.isOpened = true
            dbs.add(db)
            recentStore.doSync()
        }
    }

    fun allRecent(): List<DBSummary> {
        return dbs.toList()
    }

    fun find(uuid: String) = dbs.find { it.uuid == uuid }

}

private class DBStore(val fileName: String, val dbs: MutableList<DBSummary>) {

    private val file = File(fileName)

    fun doSync() {
        val copy = dbs.toList()
        val distinct = copy.distinctBy { it.location to it.key }
        mapper.writeValue(file, distinct)
        dbs.clear()
        dbs.addAll(distinct)
    }


    fun load(): DBStore {
        dbs.clear()
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw IllegalStateException("can't create file $fileName")
            }
        }
        try {
            val type = mapper.typeFactory.constructCollectionType(List::class.java, DBSummary::class.java)
            dbs.addAll(mapper.readValue(file, type))
        } catch(e: Exception) {
            // ignore
        }
        return this
    }

}
