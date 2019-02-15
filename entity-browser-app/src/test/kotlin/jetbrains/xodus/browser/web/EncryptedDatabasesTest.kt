package jetbrains.xodus.browser.web

import jetbrains.exodus.crypto.streamciphers.CHACHA_CIPHER_ID
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.entitystore.PersistentStoreTransaction
import jetbrains.exodus.env.EnvironmentConfig
import jetbrains.exodus.env.Environments
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

            assertNull(it.encryptionProvider)
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
                encryptionProvider = EncryptionProvider.CHACHA,
                encryptionKey = encKey,
                encryptionIV = encInit.toString()
        )
    }


    @Before
    fun setup() {
        val config = EnvironmentConfig()
                .setCipherId(CHACHA_CIPHER_ID)
                .setCipherKey(encKey)
                .setCipherBasicIV(encInit)
        val store = PersistentEntityStores.newInstance(Environments.newInstance(encStoreLocation, config), key)
        store.executeInTransaction {
            val tr = it as PersistentStoreTransaction
            store.getEntityTypeId(tr, "Type1", true)
            repeat(100) {
                tr.newEntity("Type1").also {
                    it.setProperty("type", "Band")
                }
            }
        }
        store.close()
    }

    @After
    fun cleanup() {
        File(encStoreLocation).delete()
    }

    fun newDB(db: DBSummary): DBSummary {
        return dbsResource.new(db).execute().body()!!
    }

}