package jetbrains.xodus.browser.web

import com.jetbrains.youtrack.db.api.DatabaseType
import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.orientdb.*

object EnvironmentFactory {

    fun databaseProvider(config: ODatabaseConfig, db: YouTrackDB): ODatabaseProvider {
        return ODatabaseProviderImpl(config, db)
    }

    fun connectionConfig(dbSummary: DBSummary): ODatabaseConnectionConfig {
        return ODatabaseConnectionConfig
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseType(DatabaseType.valueOf(dbSummary.type))
            .withDatabaseRoot(dbSummary.location)
            .build()
    }

    fun oDatabaseConfig(dbConnectionConfig: ODatabaseConnectionConfig, dbSummary: DBSummary): ODatabaseConfig {
        return ODatabaseConfig
            .builder()
            .withConnectionConfig(dbConnectionConfig)
            .withDatabaseName(dbSummary.key ?: "db")
            .withDatabaseType(DatabaseType.valueOf(dbSummary.type))
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
        val dbConnectionConfig = connectionConfig(dbSummary)
        val dbConfig: ODatabaseConfig = oDatabaseConfig(dbConnectionConfig, dbSummary)
        val db = iniYouTrackDb(dbConnectionConfig)
        val dbProvider = databaseProvider(dbConfig, db).apply {
            if (dbSummary.isReadonly) {
                readOnly = true
            }
        }
        return persistentEntityStore(dbProvider, dbConfig)
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
}
