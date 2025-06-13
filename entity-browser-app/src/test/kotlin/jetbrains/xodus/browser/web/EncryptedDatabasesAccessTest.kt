package jetbrains.xodus.browser.web

import YTDBDatabaseProviderFactory
import com.jetbrains.youtrack.db.api.DatabaseSession
import com.jetbrains.youtrack.db.api.DatabaseType
import com.jetbrains.youtrack.db.api.exception.RecordNotFoundException
import com.jetbrains.youtrack.db.internal.core.exception.StorageException
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseParams
import jetbrains.exodus.entitystore.youtrackdb.YTDBPersistentEntityStore
import jetbrains.exodus.entitystore.youtrackdb.createVertexClassWithClassId
import jetbrains.exodus.entitystore.youtrackdb.withSession
import org.junit.Assert
import org.junit.Test
import java.io.File

class EncryptedDatabasesAccessTest : TestSupport() {

    @Test
    fun `one db - different cipher keys`() {
        val location = newLocation()
        createDbAndPopulate(
            location = location,
            encryptionKey = "546e6f624b737371796f41586e7269304c744f42663252613630586631374a67",
            encryptionIV = 6582044510678812273L
        )
        assertDbOpenError {
            createDbAndPopulate(
                location = location,
                encryptionKey = "15e9d57c49098fb3a34763acb81c34c735f4f231f1fa7fa9b74be385269c9b81",
                encryptionIV = -5842344510678812273L
            )
        }
        assertDbOpenError {
            createDbAndPopulate(
                location = location,
                encryptionKey = null,
                encryptionIV = null
            )
        }
        File(location).delete()
    }

    private fun assertDbOpenError(accessDb: () -> Unit) {
        fun logDbInitializationFailureMessage() {
            logger.info("As expected DB failed to initialize without key")
        }

        try {
            accessDb()
            Assert.fail("Should not open")
        } catch (_: StorageException) {
            logDbInitializationFailureMessage()
        } catch (_: RecordNotFoundException) {
            logDbInitializationFailureMessage()
        } catch (_: AssertionError) {
            logDbInitializationFailureMessage()
        } catch (e: Throwable) {
            logger.error("DB failed with unexpected error", e)
            Assert.fail("Wrong error")
        }
    }

    private fun createDbAndPopulate(location: String, encryptionKey: String?, encryptionIV: Long?) {
        val params = YTDBDatabaseParams
            .builder()
            .withPassword("admin")
            .withUserName("admin")
            .withDatabaseName("encrypted-db")
            .withDatabaseType(DatabaseType.DISK)
            .withDatabasePath(location)
            .withCloseDatabaseInDbProvider(true)
            .apply {
                if (encryptionKey != null && encryptionIV != null) {
                    withHexEncryptionKey(encryptionKey, encryptionIV)
                }
            }
            .build()

        val dbProvider = YTDBDatabaseProviderFactory.createProvider(params)
        dbProvider.withSession { session: DatabaseSession ->
            val existingClass = session.schema.getClass("Type1")
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

}
