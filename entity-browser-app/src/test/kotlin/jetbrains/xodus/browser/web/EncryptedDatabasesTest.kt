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

    @Before
    fun setup() {
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
    }

    @After
    fun cleanup() {
        File(location).delete()
    }

    @Test
    fun `should be able to add new encrypted db`() {
        val encParams = newEncDBParams()
        val dbSummary = encParams.asSummary(isOpened = true)
        val newDbSummary = dbsResource.new(dbSummary).execute().body()!!
        assertTrue(newDbSummary.isOpened)
        assertTrue(newDbSummary.isEncrypted)
        assertEquals(location, newDbSummary.location)
        assertEquals(dbName, newDbSummary.key)
        assertNull(newDbSummary.encryptionKey)
        assertNull(newDbSummary.encryptionIV)
    }

    @Test
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

    private fun EnvironmentParameters.asSummary(isOpened: Boolean): DBSummary {
        return DBSummary(
            uuid = this.key,
            key = this.key,
            location = this.location,
            isOpened = isOpened,
            isReadonly = this.isReadonly,
            isWatchReadonly = false,
            isEncrypted = this.isEncrypted,
            encryptionIV = this.encryptionIV,
            encryptionKey = this.encryptionKey
        )
    }
}