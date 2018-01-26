package com.lehvolk.xodus.web.db


import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.search.smartSearch
import jetbrains.exodus.entitystore.*
import jetbrains.exodus.env.Environments
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStream

class StoreService(requisites: XodusStoreRequisites) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val store: PersistentEntityStoreImpl

    init {
        try {
            store = PersistentEntityStores.newInstance(Environments.newInstance(requisites.location), requisites.key)
        } catch (e: RuntimeException) {
            val msg = "Can't get valid Xodus entity store location and store key. Check the configuration"
            log.error(msg, e)
            throw IllegalStateException(msg, e)
        }
    }

    fun destroy() {
        var proceed = true
        var count = 1
        while (proceed && count <= 10) {
            try {
                log.info("trying to close persistent store. attempt {}", count)
                store.close()
                proceed = false
                log.info("persistent store closed")
            } catch (e: RuntimeException) {
                log.error("error closing persistent store", e)
                count++
            }
        }
    }

    fun addType(type: String): Int {
        return transactional {
            store.getEntityTypeId(it, type, true)
        }
    }

    fun allTypes(): Array<EntityType> {
        return readonly { tx -> tx.entityTypes.map { it.asEntityType(tx.store) }.sortedBy { it.name }.toTypedArray() }
    }

    fun searchType(typeId: Int, term: String?, offset: Int, pageSize: Int): SearchPager {
        return readonly { t ->
            val type = store.getEntityType(t, typeId)
            val result = smartSearch(term, type, typeId, t)
            val totalCount = result.size()
            val items = result.skip(offset).take(pageSize).map { it.asView() }
            SearchPager(items.toTypedArray(), totalCount)
        }
    }

    fun searchEntity(typeId: Int, entityId: Long, linkName: String, offset: Int, pageSize: Int): LinkPager {
        return readonly { t ->
            getEntity(typeId, entityId, t).linkView(linkName, offset, pageSize)
        }
    }

    fun newEntity(typeId: Int, vo: ChangeSummary): EntityView {
        val entityId = transactional { t ->
            val type = store.getEntityType(t, typeId)
            val entity = t.newEntity(type)
            vo.properties.forEach {
                it.newValue?.let {
                    entity.applyValues(it)
                }
            }
            vo.links.forEach {
                it.newValue?.let {
                    val link = getEntity(it.typeId, it.entityId, t)
                    entity.addLink(it.name!!, link)
                }
            }
            entity.id.localId
        }
        return getEntity(typeId, entityId)
    }

    @Throws(IOException::class)
    fun getBlob(typeId: Int, entityId: Long, blobName: String, out: OutputStream) {
        val tx = store.beginReadonlyTransaction()
        try {
            val entity = getEntity(typeId, entityId, tx)
            entity.getBlob(blobName)?.copyTo(out, bufferSize = 4096)
        } finally {
            tx.commit()
        }
    }

    private fun PersistentEntity.applyValues(property: EntityProperty) {
        property.value = safeTrim(property.value)
        val value = property.string2value()
        if (value != null) {
            this.setProperty(property.name!!, value)
        }
    }

    fun updateEntity(typeId: Int, entityId: Long, vo: ChangeSummary): EntityView {
        val localId = transactional { t ->
            val entity = getEntity(typeId, entityId, t)
            vo.properties.forEach {
                val newValue = it.newValue
                if (newValue == null) {
                    entity.deleteProperty(it.name!!)
                } else {
                    entity.applyValues(newValue)
                }
            }

            vo.links.forEach {
                val newValue = it.newValue
                val oldValue = it.oldValue
                if (newValue == null) {
                    if (oldValue != null) {
                        val linked = getEntity(oldValue.typeId, oldValue.entityId, t)
                        entity.deleteLink(it.name!!, linked)
                    } else if (it.totallyRemoved) {
                        entity.setLink(it.name!!, null)
                    }
                } else {
                    val linked = getEntity(newValue.typeId, newValue.entityId, t)
                    entity.addLink(it.name!!, linked)
                }
            }
            entityId
        }
        return getEntity(typeId, localId)
    }

    fun getEntity(typeId: Int, entityId: Long): EntityView {
        return transactional { t ->
            val entity: PersistentEntity = getEntity(typeId, entityId, t)
            entity.asView()
        }
    }

    fun deleteEntity(id: Int, entityId: Long) {
        transactional {
            val entity = getEntity(id, entityId, it)
            entity.delete()
        }
    }

    fun deleteEntitiesJob(typeId: Int, term: String?): Job {
        return object : EntityBulkJob(store) {

            override fun getAffectedEntities(): EntityIterable {
                return transactional { t ->
                    val type = store.getEntityType(typeId)
                    smartSearch(term, type, typeId, t)
                }
            }

            override fun newEntitySubJob(entities: EntityIterable): Job {
                return object : Job() {

                    override fun run() {
                        transactional { t ->
                            entities.forEach { it.delete() }
                        }
                    }
                }
            }

            override fun toString(): String {
                return "Bulk delete entities job for type $typeId and query '$term'"
            }
        }
    }

    private fun getEntity(typeId: Int, entityId: Long, t: PersistentStoreTransaction): PersistentEntity {
        try {
            return t.getEntity(PersistentEntityId(typeId, entityId))
        } catch (e: RuntimeException) {
            log.error("entity not found by type '$typeId' and entityId '$entityId'", e)
            throw EntityNotFoundException(e, typeId, entityId)
        }

    }

    private fun safeTrim(value: String?): String? {
        if (value == null) {
            return null
        }
        val trimmed = value.trim { it <= ' ' }
        return if (trimmed.isEmpty()) null else trimmed
    }

    private fun Entity.has(named: Named): Boolean {
        return this.propertyNames.contains(named.name)
    }

    private fun <T> transactional(call: (PersistentStoreTransaction) -> T): T {
        return transactional(store, call)
    }

    private fun <T> readonly(call: (PersistentStoreTransaction) -> T): T {
        return readonly(store, call)
    }

}


fun <T> transactional(store: PersistentEntityStore, call: (PersistentStoreTransaction) -> T): T {
    return store.computeInTransaction { call(it as PersistentStoreTransaction) }
}

fun <T> readonly(store: PersistentEntityStore, call: (PersistentStoreTransaction) -> T): T {
    return store.computeInReadonlyTransaction { call(it as PersistentStoreTransaction) }
}
