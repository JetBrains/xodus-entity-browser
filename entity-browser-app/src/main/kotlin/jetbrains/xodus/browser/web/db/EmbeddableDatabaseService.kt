package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.EncryptionProvider

open class EmbeddableDatabaseService(open val lookup: () -> List<DBSummary>) : DatabaseService {

    override val isReadonly: Boolean
        get() = true

    override fun all(): List<DBSummary> {
        return lookup()
    }

    override fun start() {}
    override fun stop() {}

    override fun find(uuid: String): DBSummary? {
        return all().firstOrNull { it.uuid == uuid }
    }

    override fun add(dbSummary: DBSummary): DBSummary {
        throw UnsupportedOperationException("Not supported")
    }

    override fun markStarted(uuid: String, started: Boolean): DBSummary {
        throw UnsupportedOperationException("Not supported")
    }

    override fun delete(uuid: String) {
        throw UnsupportedOperationException("Not supported")
    }

    override fun deleteAll() {
        throw UnsupportedOperationException("Not supported")
    }

}

fun PersistentEntityStoreImpl.asSummary(): DBSummary {
    return DBSummary(
            uuid = this.name,
            key = this.name,
            location = this.location,
            isOpened = true,
            isReadonly = this.environment.environmentConfig.envIsReadonly,
            isEncrypted = this.environment.environmentConfig.cipherId != null,
            isWatchReadonly = false,

            encryptionProvider = this.environment.environmentConfig.cipherId?.let { EncryptionProvider.valueOf(it) },
            encryptionIV = null,
            encryptionKey = null
    )
}