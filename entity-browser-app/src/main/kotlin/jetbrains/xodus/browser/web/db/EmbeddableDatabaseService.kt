package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.xodus.browser.web.DBSummary

open class EmbeddableDatabaseService(open val lookup: () -> List<DBSummary>) : DatabaseService {

    override val isReadonly: Boolean
        get() = true

    override fun all(): List<DBSummary> {
        return lookup()
    }

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

fun PersistentEntityStore.asSummary(forcedReadonly: Boolean): DBSummary {
    return DBSummary(
        uuid = this.name,
        key = this.name,
        location = this.location,
        isOpened = true,
        isReadonly = forcedReadonly || isReadOnly,
    )
}

fun DBSummary.asParameters(): YTDBEnvironmentParameters {
    return YTDBEnvironmentParameters(
        key = this.key,
        location = this.location,
        isReadonly = this.isReadonly,
        isEncrypted = this.isEncrypted,
        encryptionIV = this.encryptionIV,
        encryptionKey = this.encryptionKey
    )
}