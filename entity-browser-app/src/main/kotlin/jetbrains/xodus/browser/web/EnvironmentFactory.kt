package jetbrains.xodus.browser.web

import com.jetbrains.youtrack.db.api.DatabaseType
import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.orientdb.ODatabaseConfig
import jetbrains.exodus.entitystore.orientdb.ODatabaseConnectionConfig
import jetbrains.exodus.entitystore.orientdb.ODatabaseProvider
import jetbrains.exodus.entitystore.orientdb.ODatabaseProviderImpl
import jetbrains.exodus.entitystore.orientdb.OPersistentEntityStore
import jetbrains.exodus.entitystore.orientdb.OSchemaBuddyImpl
import jetbrains.exodus.entitystore.orientdb.iniYouTrackDb

object EnvironmentFactory {

    fun databaseProvider(config: ODatabaseConfig, db: YouTrackDB): ODatabaseProvider {
        return ODatabaseProviderImpl(config, db)
    }

    fun connectionConfig(location: String): ODatabaseConnectionConfig {
        return ODatabaseConnectionConfig
            .builder()
            .withPassword("hello")
            .withUserName("admin")
            .withDatabaseType(DatabaseType.PLOCAL)
            .withDatabaseRoot(location)
            .build()
    }

    fun oDatabaseConfig(dbConnectionConfig: ODatabaseConnectionConfig, dbSummary: DBSummary): ODatabaseConfig {
        return ODatabaseConfig
            .builder()
            .withConnectionConfig(dbConnectionConfig)
            .withDatabaseName("some db")
            .withDatabaseType(DatabaseType.PLOCAL)
            .withEncryption(dbSummary)
            .build()
    }

    fun persistentEntityStore(dbProvider: ODatabaseProvider, dbConfig: ODatabaseConfig): OPersistentEntityStore {
        return OPersistentEntityStore(
            databaseProvider = dbProvider,
            name = dbConfig.databaseName,
            schemaBuddy = OSchemaBuddyImpl(dbProvider, autoInitialize = false)
        )
    }

    fun persistentEntityStore(dbSummary: DBSummary): OPersistentEntityStore {
        val dbConnectionConfig = connectionConfig(dbSummary.location)
        val dbConfig: ODatabaseConfig = oDatabaseConfig(dbConnectionConfig, dbSummary)
        val db = iniYouTrackDb(dbConnectionConfig)
        val dbProvider = databaseProvider(dbConfig, db).apply {
            if (dbSummary.isReadonly) {
                readOnly = true
            }
        }
        return persistentEntityStore(dbProvider, dbConfig)
    }
}

private fun ODatabaseConfig.Builder.withEncryption(dbSummary: DBSummary): ODatabaseConfig.Builder {
    if (!dbSummary.isEncrypted) return this

    val encryptionKey = dbSummary.encryptionKey ?: throw InvalidCipherParametersException()
    val encryptionIVStr = dbSummary.encryptionIV ?: throw InvalidCipherParametersException()
    val cipherBasicIV: Long = try {
        encryptionIVStr.toLong()
    } catch (_: Exception) {
        throw InvalidCipherParametersException()
    }
    return withStringHexAndIV(encryptionKey, cipherBasicIV)
}
