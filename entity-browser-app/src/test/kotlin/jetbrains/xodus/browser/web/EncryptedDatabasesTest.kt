package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class EncryptedDatabasesTest : TestSupport() {

    private val encStoreLocation = newLocation()
    private val encKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b81"
    private val encInit = -5842344510678812273L

    @Test
    fun `should be able to add new encrypted db`() {
        val params = newEncDBParams()
        newDB(params.asSummary()).let {
            assertTrue(it.isOpened)
            assertTrue(it.isEncrypted)

            assertEquals(encStoreLocation, it.location)
            assertEquals(key, it.key)
            assertNull(it.encryptionKey)
            assertNull(it.encryptionIV)
        }
    }

    @Test
    fun `should not be able to add new encrypted db with incorrect params`() {
        val params = newEncDBParams().apply {
            encryptionKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b82"
        }
        val response = dbsResource.new(params.asSummary()).execute()
        assertEquals(400, response.code())
        assertTrue(webApp.allServices.isEmpty())
    }

    private fun newEncDBParams(): EnvironmentParameters {
        return EnvironmentParameters(
                location = encStoreLocation,
                key = key,
                isEncrypted = true,
                isReadonly = false,
                encryptionKey = encKey,
                encryptionIV = encInit.toString()
        )
    }


    @Before
    fun setup() {
        val environment = EnvironmentFactory.createEnvironment(newEncDBParams())
        environment.dbProvider.getOrCreateEntityType("Type1")
        environment.transactional { txn: StoreTransaction ->
            repeat(100) {
                txn.newEntity("Type1").also { entity ->
                    entity.setProperty("type", "Band")
                }
            }
        }
        EnvironmentFactory.closeEnvironment(environment)
    }

    @After
    fun cleanup() {
        File(encStoreLocation).delete()
    }

    fun newDB(db: DBSummary): DBSummary {
        return dbsResource.new(db).execute().body()!!
    }

}