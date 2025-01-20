package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.YouTrackDB
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.orientdb.*

object EnvironmentFactory {

    private fun databaseProvider(config: ODatabaseConfig, db: YouTrackDB, parameters: EnvironmentParameters): ODatabaseProvider {
        return ODatabaseProviderImpl(config, db).apply {
            if (parameters.isReadonly) {
                readOnly = true
            }
        }
    }

    private fun connectionConfig(parameters: EnvironmentParameters): ODatabaseConnectionConfig {
        return ODatabaseConnectionConfig
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseType(parameters.type)
            .withDatabaseRoot(parameters.location)
            .build()
    }

    private fun oDatabaseConfig(dbConnectionConfig: ODatabaseConnectionConfig, parameters: EnvironmentParameters): ODatabaseConfig {
        return ODatabaseConfig
            .builder()
            .withConnectionConfig(dbConnectionConfig)
            .withDatabaseName(parameters.key)
            .withDatabaseType(parameters.type)
            .withCloseDatabaseInDbProvider(parameters.withCloseDatabaseInDbProvider)
            .withEncryption(parameters)
            .build()
    }

    private fun ODatabaseConfig.Builder.withEncryption(parameters: EnvironmentParameters): ODatabaseConfig.Builder {
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

    fun createEnvironment(parameters: EnvironmentParameters, initializeSchema: (ODatabaseProvider.() -> Unit)? = null): Environment {
        val dbConnectionConfig = connectionConfig(parameters)
        val dbConfig: ODatabaseConfig = oDatabaseConfig(dbConnectionConfig, parameters)
        val db = iniYouTrackDb(dbConnectionConfig)
        val dbProvider: ODatabaseProvider = databaseProvider(dbConfig, db, parameters)
        initializeSchema?.invoke(dbProvider)
        val schemaBuddy = OSchemaBuddyImpl(dbProvider, autoInitialize = true)
        val store = OPersistentEntityStore(dbProvider, dbConfig.databaseName, schemaBuddy = schemaBuddy)
        return Environment(dbConfig, dbConnectionConfig, dbProvider, db, store)
    }

    fun closeEnvironment(environment: Environment) {
        environment.store.close()
        environment.dbProvider.close()
    }
}
