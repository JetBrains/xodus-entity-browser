package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.youtrackdb.*

object EnvironmentFactory {

    private fun databaseProvider(config: YTDBDatabaseConfig, db: YouTrackDB, parameters: EnvironmentParameters): YTDBDatabaseProvider {
        return YTDBDatabaseProviderImpl(config, db).apply {
            if (parameters.isReadonly) {
                readOnly = true
            }
        }
    }

    private fun connectionConfig(parameters: EnvironmentParameters): YTDBDatabaseConnectionConfig {
        return YTDBDatabaseConnectionConfig
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseType(parameters.type)
            .withDatabaseRoot(parameters.location)
            .build()
    }

    private fun oDatabaseConfig(dbConnectionConfig: YTDBDatabaseConnectionConfig, parameters: EnvironmentParameters): YTDBDatabaseConfig {
        return YTDBDatabaseConfig
            .builder()
            .withConnectionConfig(dbConnectionConfig)
            .withDatabaseName(parameters.key)
            .withDatabaseType(parameters.type)
            .withCloseDatabaseInDbProvider(parameters.withCloseDatabaseInDbProvider)
            .withEncryption(parameters)
            .build()
    }

    private fun YTDBDatabaseConfig.Builder.withEncryption(parameters: EnvironmentParameters): YTDBDatabaseConfig.Builder {
        if (!parameters.isEncrypted) return this

        val encryptionKey = parameters.encryptionKey ?: throw InvalidCipherParametersException()
        val encryptionIVStr = parameters.encryptionIV ?: throw InvalidCipherParametersException()
        val cipherBasicIV: Long = try {
            encryptionIVStr.toLong()
        } catch (_: Exception) {
            throw InvalidCipherParametersException()
        }
        return withStringHexAndIV(encryptionKey, cipherBasicIV)
    }

    fun createEnvironment(parameters: EnvironmentParameters, initializeSchema: (YTDBDatabaseProvider.() -> Unit)? = null): Environment {
        val dbConnectionConfig = connectionConfig(parameters)
        val dbConfig: YTDBDatabaseConfig = oDatabaseConfig(dbConnectionConfig, parameters)
        val db = initYouTrackDb(dbConnectionConfig)
        val dbProvider: YTDBDatabaseProvider = databaseProvider(dbConfig, db, parameters)
        initializeSchema?.invoke(dbProvider)
        val schemaBuddy = YTDBSchemaBuddyImpl(dbProvider, autoInitialize = true)
        val store = YTDBPersistentEntityStore(dbProvider, dbConfig.databaseName, schemaBuddy = schemaBuddy)
        return Environment(dbConfig, dbConnectionConfig, dbProvider, db, store)
    }

    fun closeEnvironment(environment: Environment) {
        environment.store.close()
        environment.dbProvider.close()
    }
}
