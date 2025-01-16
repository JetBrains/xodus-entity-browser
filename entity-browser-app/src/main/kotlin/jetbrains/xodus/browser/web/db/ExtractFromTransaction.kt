package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.asOStoreTransaction
import jetbrains.exodus.entitystore.orientdb.createVertexClassWithClassId
import jetbrains.exodus.entitystore.orientdb.withCurrentOrNewSession
import jetbrains.xodus.browser.web.Environment

fun <T> Environment.transactional(call: (StoreTransaction) -> T): T {
    return store.computeInTransaction { call(it.asOStoreTransaction()) }
}

fun <T> Environment.readonlyTransactional(call: (StoreTransaction) -> T): T {
    return store.computeInReadonlyTransaction { call(it.asOStoreTransaction()) }
}

fun Environment.getOrCreateEntityTypeId(type: String, allowCreate: Boolean): Int {
    val foundTypeId = readonlyTransactional { store.getEntityTypeId(type) }
    if (!allowCreate || foundTypeId != -1) {
        return foundTypeId
    }
    dbProvider.withCurrentOrNewSession(requireNoActiveTransaction = true) { session ->
        session.createVertexClassWithClassId(type)
    }
    return readonlyTransactional { store.getEntityTypeId(type) }
}