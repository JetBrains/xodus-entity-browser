package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class EncryptedDatabasesTest : TestSupport() {

    private val location = newLocation()
    private val encKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b81"
    private val encInit = -5842344510678812273L

    @Before
    fun setup() {
        val environment = EnvironmentFactory.createEnvironment(newEncDBParams()) {
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
    }

    @After
    fun cleanup() {
        File(location).delete()
    }

    @Test
    fun `should be able to add new encrypted db`() {
        val newDbSummary = dbsResource.new(newEncDBParams().asSummary()).execute().body()!!
        assertTrue(newDbSummary.isOpened)
        assertTrue(newDbSummary.isEncrypted)
        assertEquals(location, newDbSummary.location)
        assertEquals(key, newDbSummary.key)
        assertNull(newDbSummary.encryptionKey)
        assertNull(newDbSummary.encryptionIV)
    }

    @Test
    fun `should not be able to add new encrypted db with incorrect params`() {
        val wrongEncParams = newEncDBParams().apply {
            encryptionKey = "95e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b81"
        }
        val response = dbsResource.new(wrongEncParams.asSummary()).execute()
        assertEquals(400, response.code())
        assertTrue(webApp.allServices.isEmpty())
    }

    private fun newEncDBParams(): EnvironmentParameters {
        return EnvironmentParameters(
                location = location,
                key = key,
                isEncrypted = true,
                isReadonly = false,
                encryptionKey = encKey,
                encryptionIV = encInit.toString()
        )
    }
}