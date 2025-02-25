package jetbrains.xodus.browser.web.db


import com.jetbrains.youtrack.db.api.exception.RecordNotFoundException
import com.jetbrains.youtrack.db.internal.core.exception.StorageException
import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.log.DataCorruptionException
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.DatabaseException
import mu.KLogging

class EnvironmentStoreService: AbstractStoreService {

    companion object : KLogging()

    private val environment: Environment

    override val store: PersistentEntityStore get() = environment.store
    override val isReadonly: Boolean

    constructor(dbSummary: DBSummary) {
        try {
            this.environment = YTDBEnvironmentFactory.createEnvironment(dbSummary.asParameters())
            this.isReadonly = environment.store.isReadOnly
        } catch (e: InvalidCipherParametersException) {
            handleInvalidCipherParametersException(e)
        } catch (e: DataCorruptionException) {
            handleDataCorruptionException(e)
        } catch (e: StorageException) {
            handleStorageException(e)
        } catch (e: RecordNotFoundException) {
            handleRecordNotFoundException(e)
        } catch (e: RuntimeException) {
            val msg = "Can't get valid Xodus entity store location and store key. Check the configuration"
            logger.error(e) { msg }
            throw IllegalStateException(msg, e)
        }
    }

    private fun handleInvalidCipherParametersException(e: InvalidCipherParametersException): Nothing {
        logger.error(e) {
            "It seems that store encrypted with another parameters"
        }
        throw DatabaseException("Database is ciphered with different/unknown cipher parameters")
    }

    private fun handleDataCorruptionException(e: DataCorruptionException): Nothing {
        logger.error(e) {
            "Cannot open database because of data corruption"
        }
        throw DatabaseException("Database is ciphered with different/unknown cipher parameters or corrupted")
    }

    private fun handleStorageException(e: StorageException): Nothing {
        if (e.message == "Database is locked by another process, please shutdown process and try again") {
            val msg = "Cannot open database because database is locked by another process"
            logger.error(e) { msg }
            throw IllegalStateException(msg, e)
        } else {
            logger.error(e) { "Cannot open database because of storage error" }
            throw DatabaseException("Database can not be opened with presented parameters")
        }
    }

    private fun handleRecordNotFoundException(e: RecordNotFoundException): Nothing {
        logger.error(e) {
            "Cannot open database because of missing error"
        }
        throw DatabaseException("Database can not be opened due to missing record")
    }

    override fun stop() {
        var proceed = true
        var count = 1
        while (proceed && count <= 10) {
            try {
                logger.info { "trying to close persistent store. attempt $count" }
                YTDBEnvironmentFactory.closeEnvironment(environment)
                proceed = false
                logger.info("persistent store closed")
            } catch (e: RuntimeException) {
                logger.error(e) { "error closing persistent store" }
                count++
            }
        }
    }
}