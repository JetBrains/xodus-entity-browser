package jetbrains.xodus.browser.web.db


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
            this.environment = EnvironmentFactory.createEnvironment(dbSummary.asParameters())
            this.isReadonly = environment.store.isReadOnly
        } catch (e: InvalidCipherParametersException) {
            val msg = "It seems that store encrypted with another parameters"
            logger.error(e) { msg }
            throw DatabaseException("Database is ciphered with different/unknown cipher parameters")
        } catch (e: StorageException) {
            val msg = "It seems that store can not be opened with presented parameters"
            logger.error(e) { msg }
            throw DatabaseException("Database is ciphered with different/unknown cipher parameters or corrupted")
        } catch (e: DataCorruptionException) {
            val msg = "Cannot open database because of data corruption"
            logger.error(e) { msg }
            throw DatabaseException("Database is ciphered with different/unknown cipher parameters or corrupted")
        } catch (e: RuntimeException) {
            val msg = "Can't get valid Xodus entity store location and store key. Check the configuration"
            logger.error(e) { msg }
            throw IllegalStateException(msg, e)
        }
    }

    override fun stop() {
        var proceed = true
        var count = 1
        while (proceed && count <= 10) {
            try {
                logger.info { "trying to close persistent store. attempt $count" }
                EnvironmentFactory.closeEnvironment(environment)
                proceed = false
                logger.info("persistent store closed")
            } catch (e: RuntimeException) {
                logger.error(e) { "error closing persistent store" }
                count++
            }
        }
    }
}