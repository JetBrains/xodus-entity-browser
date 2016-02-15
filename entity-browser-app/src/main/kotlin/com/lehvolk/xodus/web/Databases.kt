package com.lehvolk.xodus.web

import java.io.File
import java.util.*

object Databases {

    private val mapper = JacksonConfigurator().mapper
    private val store = File("./recent-dbs.json")
    private val dbs = fromStore()

    fun add(db: DB) {
        dbs.add(db)
        sync()
    }

    fun delete(db: DB) {
        dbs.removeAll { db.location == it.location && db.key == it.key }
        sync()
    }

    fun allRecent(): List<DB> {
        return dbs.toList()
    }

    private fun sync() {
        val copy = dbs.toList()
        val distinct = copy.distinctBy { it.location to it.key }
        mapper.writeValue(store, distinct)
        dbs.clear()
        dbs.addAll(distinct)
    }

    private fun fromStore(): MutableList<DB> {
        if (!store.exists()) {
            if (!store.createNewFile()) {
                throw IllegalStateException("can't create file ${store.name}")
            }
            return LinkedList()
        }
        try {
            val type = mapper.typeFactory.constructCollectionType(List::class.java, DB::class.java)
            return mapper.readValue(store, type)
        } catch(e: Exception) {
            return LinkedList()
        }
    }

}