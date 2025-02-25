package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseParams
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseProvider

data class Environment(
    val dbParams: YTDBDatabaseParams,
    val dbProvider: YTDBDatabaseProvider,
    val store: PersistentEntityStore
)