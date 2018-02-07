package com.lehvolk.xodus.web.db

import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.DatabaseNotFoundException
import com.lehvolk.xodus.web.mapper
import com.lehvolk.xodus.web.systemOr
import java.io.File
import java.util.*


object Databases {

    internal val file by lazy {
        File("recent.dbs" systemOr "./recent-dbs.json")
    }

    private val dbs by lazy {
        val result = arrayListOf<DBSummary>()
        val canonicalFile = File(file.canonicalPath)
        if (!canonicalFile.exists()) {
            val parent = canonicalFile.parentFile
            if (!parent.exists() && !parent.mkdirs()) {
                throw IllegalStateException("can't create folder ${parent.canonicalPath}")
            }
            if (!canonicalFile.createNewFile()) {
                throw IllegalStateException("can't create file ${canonicalFile.absolutePath}")
            }
        }
        try {
            val type = mapper.typeFactory.constructCollectionType(List::class.java, DBSummary::class.java)
            result.addAll(mapper.readValue(canonicalFile, type))
        } catch (e: Exception) {
            // ignore
        }
        result
    }

    fun add(location: String, key: String): DBSummary {
        val uuid = UUID.randomUUID().toString()
        saveWith {
            dbs.add(DBSummary(location, key, uuid = uuid))
        }
        return find(uuid) {
            throw DatabaseNotFoundException("Database on '$location' is already registered")
        }
    }

    fun applyChange(uuid: String, call: DBSummary.() -> Unit): DBSummary {
        val dbCopy = find(uuid) {
            throw DatabaseNotFoundException("Database not found by id '$uuid'")
        }
        val location = dbCopy.location
        saveWith {
            dbCopy.call()
        }
        return find(uuid) {
            throw DatabaseNotFoundException("Database on '$location' can't be modified")
        }
    }

    fun delete(uuid: String) {
        saveWith {
            dbs.removeAll { it.uuid == uuid }
        }
    }

    fun all(): List<DBSummary> {
        return dbs.toList()
    }

    internal fun find(uuid: String, error: () -> Nothing = {
        throw DatabaseNotFoundException("Database not found by id '$uuid'")
    }) = dbs.firstOrNull { it.uuid == uuid } ?: error()


    private fun saveWith(call: () -> Unit) {
        synchronized(this) {
            call()
            doSync()
        }
    }

    private fun doSync() {
        val copy = dbs.toList()
        val distinct = copy.distinctBy { it.location to it.key }
        mapper.writeValue(file, distinct)
        dbs.clear()
        dbs.addAll(distinct)
    }

}
