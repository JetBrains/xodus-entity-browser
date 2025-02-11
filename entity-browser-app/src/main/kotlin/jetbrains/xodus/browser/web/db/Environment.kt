package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseConfig
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseConnectionConfig
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseProvider

data class Environment(
    val dbConfig: YTDBDatabaseConfig,
    val dbConnectionConfiguration: YTDBDatabaseConnectionConfig,
    val dbProvider: YTDBDatabaseProvider,
    val db: YouTrackDB,
    val store: PersistentEntityStore
)