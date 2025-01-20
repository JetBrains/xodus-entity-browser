package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.orientdb.ODatabaseConfig
import jetbrains.exodus.entitystore.orientdb.ODatabaseConnectionConfig
import jetbrains.exodus.entitystore.orientdb.ODatabaseProvider

data class Environment(
    val dbConfig: ODatabaseConfig,
    val dbConnectionConfiguration: ODatabaseConnectionConfig,
    val dbProvider: ODatabaseProvider,
    val db: YouTrackDB,
    val store: PersistentEntityStore
)