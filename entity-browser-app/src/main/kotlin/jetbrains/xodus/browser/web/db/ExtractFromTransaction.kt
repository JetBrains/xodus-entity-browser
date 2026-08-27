package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrackdb.internal.core.metadata.schema.schema.SchemaClass
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.asYTDBTransaction
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseProvider
import jetbrains.exodus.entitystore.youtrackdb.YTDBVertexEntity
import jetbrains.exodus.entitystore.youtrackdb.createVertexClassWithClassId
import jetbrains.exodus.entitystore.youtrackdb.withTx

fun <T> Environment.transactional(call: (StoreTransaction) -> T): T {
    return store.computeInTransaction { call(it.asYTDBTransaction()) }
}

fun <T> Environment.readonlyTransactional(call: (StoreTransaction) -> T): T {
    return store.computeInReadonlyTransaction { call(it.asYTDBTransaction()) }
}

fun YTDBDatabaseProvider.getOrCreateEntityType(type: String): SchemaClass {
    return withSession { session ->
        session.withTx { tx ->
            tx.schema.getClass(type) ?: tx.createVertexClassWithClassId(type)
        }
    }
}

fun YTDBDatabaseProvider.addAssociation(
    fromClassName: String,
    toClassName: String,
    outPropName: String,
    inPropName: String
) {
    withSession { session ->
        session.withTx { tx ->
            tx.schema.getClass(fromClassName) ?: throw IllegalStateException("$fromClassName not found")
            tx.schema.getClass(toClassName) ?: throw IllegalStateException("$toClassName not found")

            val inEdgeName = YTDBVertexEntity.edgeClassName(inPropName)
            val outEdgeName = YTDBVertexEntity.edgeClassName(outPropName)
            tx.schema.getClass(inEdgeName) ?: tx.schema.createEdgeClass(inEdgeName)
            tx.schema.getClass(outEdgeName) ?: tx.schema.createEdgeClass(outEdgeName)
        }
    }
}
