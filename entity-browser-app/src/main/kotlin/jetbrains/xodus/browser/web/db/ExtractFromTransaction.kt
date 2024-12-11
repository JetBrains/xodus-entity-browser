package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.asOStoreTransaction
import jetbrains.exodus.entitystore.orientdb.*
import kotlin.reflect.full.declaredMemberProperties

private fun OStoreTransaction.extractSchemaBuddy(): OSchemaBuddy {
    val schemaBuddy = OStoreTransactionImpl::class
        .declaredMemberProperties.find { it.name == "schemaBuddy" }?.getter
        ?.call(this) as? OSchemaBuddyImpl?
    return schemaBuddy!!
}

private fun OStoreTransaction.extractDatabaseProvider(): ODatabaseProvider {
    val schemaBuddy = extractSchemaBuddy() as? OSchemaBuddyImpl?
    val dbProvider: ODatabaseProvider? = OSchemaBuddyImpl::class
        .declaredMemberProperties.find { it.name == "dbProvider" }?.getter
        ?.call(schemaBuddy!!) as? ODatabaseProvider?
    return dbProvider!!
}

private fun OStoreTransaction.extractDatabaseConfig(): ODatabaseConfig {
    val dbProvider = extractDatabaseProvider() as? ODatabaseProviderImpl?
    val dbConfig: ODatabaseConfig? = ODatabaseProviderImpl::class
        .declaredMemberProperties.find { it.name == "config" }?.getter
        ?.call(dbProvider!!) as? ODatabaseConfig?
    return dbConfig!!
}

val StoreTransaction.isEnvironmentReadOnly: Boolean
    get() {
        // TODO get property without reflection
        return this.asOStoreTransaction().extractDatabaseProvider().readOnly
    }

val StoreTransaction.isDatabaseEncrypted: Boolean
    get() {
        // TODO get property without reflection
        return this.asOStoreTransaction().extractDatabaseConfig().cipherKey != null
    }
