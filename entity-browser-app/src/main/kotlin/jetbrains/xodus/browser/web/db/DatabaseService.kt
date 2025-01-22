package jetbrains.xodus.browser.web.db

import jetbrains.xodus.browser.web.DBSummary


interface DatabaseService {

    val isReadonly: Boolean

    fun all(): List<DBSummary>

    fun find(uuid: String): DBSummary?

    fun add(dbSummary: DBSummary): DBSummary

    fun markStarted(uuid: String, started: Boolean): DBSummary

    fun delete(uuid: String)

    fun deleteAll()
}