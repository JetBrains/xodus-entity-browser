package jetbrains.xodus.browser.web

import com.jetbrains.youtrack.db.api.DatabaseType
import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.orientdb.*

object EnvironmentFactory {

    private fun databaseProvider(config: ODatabaseConfig, db: YouTrackDB, dbSummary: DBSummary): ODatabaseProvider {
        return ODatabaseProviderImpl(config, db).apply {
            if (dbSummary.isReadonly) {
                readOnly = true
            }
        }
    }

    private fun connectionConfig(dbSummary: DBSummary): ODatabaseConnectionConfig {
        return ODatabaseConnectionConfig
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseType(DatabaseType.valueOf(dbSummary.type))
            .withDatabaseRoot(dbSummary.location)
            .build()
    }

    private fun oDatabaseConfig(dbConnectionConfig: ODatabaseConnectionConfig, dbSummary: DBSummary): ODatabaseConfig {
        return ODatabaseConfig
            .builder()
            .withConnectionConfig(dbConnectionConfig)
            .withDatabaseName(dbSummary.key ?: "db")
            .withDatabaseType(DatabaseType.valueOf(dbSummary.type))
            .withEncryption(dbSummary)
            .build()
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

    private fun persistentEntityStore(dbProvider: ODatabaseProvider, dbConfig: ODatabaseConfig): OPersistentEntityStore {
        return OPersistentEntityStore(
            databaseProvider = dbProvider,
            name = dbConfig.databaseName,
            schemaBuddy = OSchemaBuddyImpl(dbProvider, autoInitialize = false)
        )
    }

    fun environment(dbSummary: DBSummary): Environment {
        val dbConnectionConfig = connectionConfig(dbSummary)
        val dbConfig: ODatabaseConfig = oDatabaseConfig(dbConnectionConfig, dbSummary)
        val db = iniYouTrackDb(dbConnectionConfig)
        val dbProvider = databaseProvider(dbConfig, db, dbSummary)
        val store = persistentEntityStore(dbProvider, dbConfig)
        return Environment(dbConfig, dbConnectionConfig, dbProvider, db, store)
    }
}
