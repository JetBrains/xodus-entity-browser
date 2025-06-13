package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.DatabaseSession
import com.jetbrains.youtrack.db.api.record.Direction
import com.jetbrains.youtrack.db.api.record.Vertex
import com.jetbrains.youtrack.db.api.schema.PropertyType
import com.jetbrains.youtrack.db.api.schema.SchemaClass
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.asOStoreTransaction
import jetbrains.exodus.entitystore.youtrackdb.YTDBDatabaseProvider
import jetbrains.exodus.entitystore.youtrackdb.YTDBVertexEntity
import jetbrains.exodus.entitystore.youtrackdb.createVertexClassWithClassId
import jetbrains.exodus.entitystore.youtrackdb.withSession

fun <T> Environment.transactional(call: (StoreTransaction) -> T): T {
    return store.computeInTransaction { call(it.asOStoreTransaction()) }
}

fun <T> Environment.readonlyTransactional(call: (StoreTransaction) -> T): T {
    return store.computeInReadonlyTransaction { call(it.asOStoreTransaction()) }
}

fun YTDBDatabaseProvider.getOrCreateEntityType(type: String): SchemaClass {
    return withSession { session: DatabaseSession ->
        session.createVertexClassWithClassId(type)
    }
}

fun YTDBDatabaseProvider.addAssociation(
    fromClassName: String,
    toClassName: String,
    outPropName: String,
    inPropName: String
) {
    withSession { session: DatabaseSession ->
        val fromClass = session.schema.getClass(fromClassName) ?: throw IllegalStateException("$fromClassName not found")
        val toClass = session.schema.getClass(toClassName) ?: throw IllegalStateException("$toClassName not found")
        val inEdgeName = YTDBVertexEntity.edgeClassName(inPropName)
        val outEdgeName = YTDBVertexEntity.edgeClassName(outPropName)
        session.schema.getClass(inEdgeName) ?: session.schema.createEdgeClass(inEdgeName)
        session.schema.getClass(outEdgeName) ?: session.schema.createEdgeClass(outEdgeName)
        val linkInPropName = Vertex.getEdgeLinkFieldName(Direction.IN, inEdgeName)
        val linkOutPropName = Vertex.getEdgeLinkFieldName(Direction.OUT, outEdgeName)
        fromClass.createProperty(linkOutPropName, PropertyType.LINKBAG)
        toClass.createProperty(linkInPropName, PropertyType.LINKBAG)
    }
}
