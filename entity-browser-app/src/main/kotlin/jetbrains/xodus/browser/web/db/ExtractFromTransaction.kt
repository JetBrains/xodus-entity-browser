package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.DatabaseSession
import com.jetbrains.youtrack.db.api.record.Direction
import com.jetbrains.youtrack.db.api.record.Vertex
import com.jetbrains.youtrack.db.api.schema.PropertyType
import com.jetbrains.youtrack.db.api.schema.SchemaClass
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.asOStoreTransaction
import jetbrains.exodus.entitystore.orientdb.*

fun <T> Environment.transactional(call: (StoreTransaction) -> T): T {
    return store.computeInTransaction { call(it.asOStoreTransaction()) }
}

fun <T> Environment.readonlyTransactional(call: (StoreTransaction) -> T): T {
    return store.computeInReadonlyTransaction { call(it.asOStoreTransaction()) }
}

fun ODatabaseProvider.getOrCreateEntityType(type: String): SchemaClass {
    return withSession { session: DatabaseSession ->
        session.createVertexClassWithClassId(type)
    }
}

fun ODatabaseProvider.addAssociation(
    fromClassName: String,
    toClassName: String,
    outPropName: String,
    inPropName: String
) {
    withSession { session: DatabaseSession ->
        val fromClass = session.getClass(fromClassName) ?: throw IllegalStateException("$fromClassName not found")
        val toClass = session.getClass(toClassName) ?: throw IllegalStateException("$toClassName not found")
        val inEdgeName = OVertexEntity.edgeClassName(inPropName)
        val outEdgeName = OVertexEntity.edgeClassName(outPropName)
        session.getClass(inEdgeName) ?: session.createEdgeClass(inEdgeName)
        session.getClass(outEdgeName) ?: session.createEdgeClass(outEdgeName)
        val linkInPropName = Vertex.getEdgeLinkFieldName(Direction.IN, inEdgeName)
        val linkOutPropName = Vertex.getEdgeLinkFieldName(Direction.OUT, outEdgeName)
        fromClass.createProperty(session, linkOutPropName, PropertyType.LINKBAG)
        toClass.createProperty(session, linkInPropName, PropertyType.LINKBAG)
    }
}