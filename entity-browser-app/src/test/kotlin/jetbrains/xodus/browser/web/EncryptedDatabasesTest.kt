package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.xodus.browser.web.db.YTDBEnvironmentFactory
import jetbrains.xodus.browser.web.db.YTDBEnvironmentParameters
import jetbrains.xodus.browser.web.db.getOrCreateEntityType
import jetbrains.xodus.browser.web.db.transactional
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class EncryptedDatabasesTest : TestSupport() {

    private fun newEncDBParams(location: String): YTDBEnvironmentParameters {
        return YTDBEnvironmentParameters(
            location = location,
            key = "encrypted-db",
            isEncrypted = true,
            isReadonly = false,
            encryptionKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b81",
            encryptionIV = (-5842344510678812273L).toString()
        )
    }

    private fun createAndPopulateDb(params: YTDBEnvironmentParameters) {
        val environment = YTDBEnvironmentFactory.createEnvironment(params) {
            getOrCreateEntityType("Type1")
        }
        environment.transactional { txn: StoreTransaction ->
            repeat(100) {
                txn.newEntity("Type1").also { entity ->
                    entity.setProperty("type", "Band")
                }
            }
        }
        YTDBEnvironmentFactory.closeEnvironment(environment)
    }

    private fun cleanupDb(location: String) {
        try {
            File(location).delete()
        } catch (e: Exception) {
            logger.error("can't delete file", e)
        }
    }

    @Test
    fun `should be able to add new encrypted db`() {
        val location = newLocation()
        val encParams = newEncDBParams(location)
        createAndPopulateDb(encParams)

        val dbSummary = encParams.asSummary(isOpened = true)
        val newDbSummary = dbsResource.new(dbSummary).execute().body()!!
        assertTrue(newDbSummary.isOpened)
        assertTrue(newDbSummary.isEncrypted)
        assertEquals(location, newDbSummary.location)
        assertEquals(encParams.key, newDbSummary.key)
        assertNull(newDbSummary.encryptionKey)
        assertNull(newDbSummary.encryptionIV)

        cleanupDb(location)
    }

    @Test
    fun `should not be able to add new encrypted db with incorrect params`() {
        val location = newLocation()
        val encParams = newEncDBParams(location)
        createAndPopulateDb(encParams)

        val wrongEncParams = newEncDBParams(location).apply {
            encryptionKey = "0000057c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be38526900000"
            encryptionIV = (-5000044510678810000L).toString()
        }
        val wrongDbSummary = wrongEncParams.asSummary(isOpened = true)
        val response = dbsResource.new(wrongDbSummary).execute()
        assertEquals(400, response.code())
        assertTrue(webApp.allServices.isEmpty())

        cleanupDb(location)
    }

    private fun YTDBEnvironmentParameters.asSummary(isOpened: Boolean): DBSummary {
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