package jetbrains.xodus.browser.web.db

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

fun Environment.asSummary(forcedReadonly: Boolean): DBSummary {
    return DBSummary(
        uuid = store.name,
        key = store.name,
        location = store.location,
        isOpened = true,
        isReadonly = forcedReadonly || dbProvider.readOnly,
        isWatchReadonly = false,
        isEncrypted = dbConfig.cipherKey != null,
        encryptionIV = null,
        encryptionKey = null
    )
}

fun EnvironmentParameters.asSummary(): DBSummary {
    return DBSummary(
        uuid = this.key,
        key = this.key,
        location = this.location,
        isOpened = true,
        isReadonly = this.isReadonly,
        isWatchReadonly = false,
        isEncrypted = this.isEncrypted,
        encryptionIV = this.encryptionIV,
        encryptionKey = this.encryptionKey
    )
}

fun DBSummary.asParameters(): EnvironmentParameters {
    return EnvironmentParameters(
        key = this.key ?: "db",
        location = this.location,
        isReadonly = this.isReadonly,
        isEncrypted = this.isEncrypted,
        encryptionIV = this.encryptionIV,
        encryptionKey = this.encryptionKey
    )
}