package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.PersistentStoreTransaction
import jetbrains.xodus.browser.web.db.getOrCreateEntityTypeId
import jetbrains.xodus.browser.web.db.transactional
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
        val db = newEncDB()
        newDB(db).let {
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
        val db = newEncDB().apply {
            encryptionKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b82"
        }
        val response = dbsResource.new(db).execute()
        assertEquals(400, response.code())
        assertTrue(webApp.allServices.isEmpty())
    }

    private fun newEncDB(): DBSummary {
        return DBSummary(
                location = encStoreLocation,
                key = key,
                isOpened = true,
                isEncrypted = true,
                isReadonly = false,
                isWatchReadonly = false,
                encryptionKey = encKey,
                encryptionIV = encInit.toString()
        )
    }


    @Before
    fun setup() {
        val dbSummary = newEncDB()
        val environment = EnvironmentFactory.environment(dbSummary)
        environment.getOrCreateEntityTypeId("Type1", allowCreate = true)
        environment.transactional {
            val tr = it as PersistentStoreTransaction
            repeat(100) {
                tr.newEntity("Type1").also {
                    it.setProperty("type", "Band")
                }
            }
        }
        environment.store.close()
    }

    @After
    fun cleanup() {
        File(encStoreLocation).delete()
    }

    fun newDB(db: DBSummary): DBSummary {
        return dbsResource.new(db).execute().body()!!
    }

}