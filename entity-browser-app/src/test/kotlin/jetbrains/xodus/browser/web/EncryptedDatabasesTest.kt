package jetbrains.xodus.browser.web

import com.jetbrains.youtrack.db.api.DatabaseSession
import com.jetbrains.youtrack.db.api.DatabaseType
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseConfig
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseConnectionConfig
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseProviderImpl
import jetbrains.exodus.entitystore.youtrackdb.YTDBPersistentEntityStore
import jetbrains.exodus.entitystore.youtrackdb.createVertexClassWithClassId
import jetbrains.exodus.entitystore.youtrackdb.initYouTrackDb
import jetbrains.exodus.entitystore.youtrackdb.withSession
import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File

class EncryptedDatabasesTest : TestSupport() {

    private val location = newLocation()
    private val dbName = "encrypted-db"
    private val encKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b81"
    private val encInit = -5842344510678812273L

    private fun newEncDBParams(): EnvironmentParameters {
        return EnvironmentParameters(
            location = location,
            key = dbName,
            isEncrypted = true,
            isReadonly = false,
            encryptionKey = encKey,
            encryptionIV = encInit.toString()
        )
    }

    @Test
    fun `should be able to add new encrypted db`() {
        val encParams = newEncDBParams()
        val environment = EnvironmentFactory.createEnvironment(encParams) {
            getOrCreateEntityType("Type1")
        }
        environment.transactional { txn: StoreTransaction ->
            repeat(100) {
                txn.newEntity("Type1").also { entity ->
                    entity.setProperty("type", "Band")
                }
            }
        }
        EnvironmentFactory.closeEnvironment(environment)

        val dbSummary = encParams.asSummary(isOpened = true)
        val newDbSummary = dbsResource.new(dbSummary).execute().body()!!
        assertTrue(newDbSummary.isOpened)
        assertTrue(newDbSummary.isEncrypted)
        assertEquals(location, newDbSummary.location)
        assertEquals(dbName, newDbSummary.key)
        assertNull(newDbSummary.encryptionKey)
        assertNull(newDbSummary.encryptionIV)
        File(location).delete()
    }

    @Test
    @Ignore("test temporarily disabled, as it waits for the fix of YTDB-251")
    fun `should not be able to add new encrypted db with incorrect params`() {
        val wrongEncParams = newEncDBParams().apply {
            encryptionKey = "0000057c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be38526900000"
            encryptionIV = (-5000044510678810000L).toString()
        }
        val wrongDbSummary = wrongEncParams.asSummary(isOpened = true)
        val response = dbsResource.new(wrongDbSummary).execute()
        assertEquals(400, response.code())
        assertTrue(webApp.allServices.isEmpty())
    }

    @Test
    fun `one db - two different cipher keys`() {
        val location = newLocation()
        createDbAndPopulate(
            location = location,
            encryptionKey = "546e6f624b737371796f41586e7269304c744f42663252613630586631374a67",
            encryptionIV = 0L
        )
        try {
            createDbAndPopulate(
                location = location,
                encryptionKey = null,
                encryptionIV = null
            )
        } catch (e: Exception) {
            logger.error("error", e)
        }
        File(location).delete()
    }

    private fun createDbAndPopulate(location: String, encryptionKey: String?, encryptionIV: Long?) {
        val dbConnectionConfig = YTDBDatabaseConnectionConfig
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseType(DatabaseType.PLOCAL)
            .withDatabaseRoot(location)
            .build()

        val dbConfig = YTDBDatabaseConfig
            .builder()
            .withConnectionConfig(dbConnectionConfig)
            .withDatabaseName("encrypted-db")
            .withDatabaseType(DatabaseType.PLOCAL)
            .withCloseDatabaseInDbProvider(true)
            .apply {
                if (encryptionKey != null && encryptionIV != null) {
                    withStringHexAndIV(encryptionKey, encryptionIV)
                }
            }
            .build()

        val db = initYouTrackDb(dbConnectionConfig)
        val dbProvider = YTDBDatabaseProviderImpl(dbConfig, db)
        dbProvider.withSession { session: DatabaseSession ->
            val existingClass = session.getClass("Type1")
            if (existingClass == null) {
                session.createVertexClassWithClassId("Type1")
            }
        }
        val store = YTDBPersistentEntityStore(dbProvider, "encrypted-db")
        store.computeInTransaction { txn: StoreTransaction ->
            repeat(100) {
                txn.newEntity("Type1").also { entity ->
                    entity.setProperty("type", "Band")
                }
            }
        }
        store.close()
        dbProvider.close()
    }

    private fun EnvironmentParameters.asSummary(isOpened: Boolean): DBSummary {
        return DBSummary(
            uuid = this.key,
            key = this.key,
            location = this.location,
            isOpened = isOpened,
            isReadonly = this.isReadonly,
            isEncrypted = this.isEncrypted,
            encryptionIV = this.encryptionIV,
            encryptionKey = this.encryptionKey
        )
    }
}