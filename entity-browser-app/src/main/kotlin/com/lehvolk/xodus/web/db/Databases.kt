package com.lehvolk.xodus.web.db

import com.lehvolk.xodus.web.DBSummary
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
            dbs.add(DBSummary().also {
                it.location = location
                it.key = key
                it.uuid = uuid
            })
        }
        return find(uuid)
    }

    fun delete(uuid: String) {
        saveWith {
            dbs.removeAll { it.uuid == uuid }
        }
    }

    fun markUnavailable(uuid: String) {
        saveWith {
            dbs.first { it.uuid == uuid }.isOpened = false
        }
    }

    fun open(uuid: String) {
        saveWith {
            dbs.first { it.uuid == uuid }.isOpened = true
        }
    }

    fun all(): List<DBSummary> {
        return dbs.toList()
    }

    fun deleteAll() {
        saveWith {
            dbs.clear()
        }
    }

    fun find(uuid: String) = dbs.first { it.uuid == uuid }.apply {
        DBSummary().also {
            it.location = location
            it.key = key
            it.uuid = uuid
        }
    }

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
