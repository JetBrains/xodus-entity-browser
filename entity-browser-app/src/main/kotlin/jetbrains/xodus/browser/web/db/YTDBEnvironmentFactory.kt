package jetbrains.xodus.browser.web.db

import YTDBDatabaseProviderFactory
import YouTrackDBFactory
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseParams
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseProvider
import jetbrains.exodus.entitystore.youtrackdb.YTDBPersistentEntityStore
import jetbrains.exodus.entitystore.youtrackdb.YTDBSchemaBuddyImpl

object YTDBEnvironmentFactory {

    private fun createDatabaseParameters(parameters: YTDBEnvironmentParameters): YTDBDatabaseParams {
        return YTDBDatabaseParams
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseName(parameters.key)
            .withDatabaseType(parameters.type)
            .withDatabasePath(parameters.location)
            .withCloseDatabaseInDbProvider(parameters.withCloseDatabaseInDbProvider)
            .withEncryption(parameters)
            .build()
    }

    private fun YTDBDatabaseParams.Builder.withEncryption(parameters: YTDBEnvironmentParameters): YTDBDatabaseParams.Builder {
        if (!parameters.isEncrypted) return this

        val encryptionKey = parameters.encryptionKey ?: throw InvalidCipherParametersException()
        val encryptionIVStr = parameters.encryptionIV ?: throw InvalidCipherParametersException()
        val cipherBasicIV: Long = try {
            encryptionIVStr.toLong()
        } catch (_: Exception) {
            throw InvalidCipherParametersException()
        }
        return withHexEncryptionKey(encryptionKey, cipherBasicIV)
    }

    fun createEnvironment(parameters: YTDBEnvironmentParameters, initializeSchema: (YTDBDatabaseProvider.() -> Unit)? = null): Environment {
        val dbParams = createDatabaseParameters(parameters)
        val db = YouTrackDBFactory.createEmbedded(dbParams)
        val dbProvider = YTDBDatabaseProviderFactory.createProvider(dbParams, db)
        initializeSchema?.invoke(dbProvider)
        val schemaBuddy = YTDBSchemaBuddyImpl(dbProvider, autoInitialize = true)
        val store = YTDBPersistentEntityStore(dbProvider, dbParams.databaseName, schemaBuddy = schemaBuddy)
        if (parameters.isReadonly) {
            dbProvider.readOnly = true
        }
        return Environment(dbParams, dbProvider, store)
    }

    fun closeEnvironment(environment: Environment) {
        environment.store.close()
        environment.dbProvider.close()
    }
}
