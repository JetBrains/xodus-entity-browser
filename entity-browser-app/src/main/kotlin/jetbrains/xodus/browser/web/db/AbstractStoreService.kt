package jetbrains.xodus.browser.web.db


import jetbrains.exodus.entitystore.*
import jetbrains.xodus.browser.web.*
import jetbrains.xodus.browser.web.search.smartSearch
import mu.KLogging
import java.io.IOException
import java.io.InputStream

abstract class AbstractStoreService: StoreService {

    companion object : KLogging()

    protected abstract val store: PersistentEntityStore

    override fun addType(type: String): Int {
        throw UnsupportedOperationException("Adding a new type is not allowed for a database with an already initialized model")
    }

    override fun allTypes(): Array<EntityType> {
        return readonly { txn ->
            txn.entityTypes
                .sorted()
                .map { typeName -> EntityType(store.getEntityTypeId(typeName), typeName) }
                .toTypedArray()
        }
    }

    override fun searchType(typeId: Int, q: String?, offset: Int, pageSize: Int): SearchPager {
        return readonly { t ->
            val type = store.getEntityType(typeId)
            val result = smartSearch(q, type, typeId, t)
            val totalCount = result.size()
            val items = result.skip(offset).take(pageSize).map { it.asView() }
            SearchPager(items.toList(), totalCount)
        }
    }

    override fun searchEntity(id: String, linkName: String, offset: Int, pageSize: Int): LinkPager {
        return readonly { t ->
            getEntity(id, t).linkView(linkName, offset, pageSize)
        }
    }

    override fun newEntity(typeId: Int, vo: ChangeSummary): EntityView {
        val entityId = transactional { t: StoreTransaction ->
            val type = store.getEntityType(typeId)
            val entity = t.newEntity(type)
            vo.properties.forEach {
                it.newValue?.let { newValue ->
                    entity.applyValues(newValue)
                }
            }
            vo.links.forEach { linkChange ->
                linkChange.newValue?.let { newValue ->
                    val link = getEntity(newValue.id, t)
                    entity.addLink(newValue.name, link)
                }
            }
            entity.toIdString()
        }
        return getEntity(entityId)
    }

    @Throws(IOException::class)
    override fun getBlob(id: String, blobName: String): InputStream {
        return readonly {
            val entity = getEntity(id, it)
            entity.getBlob(blobName) ?: throw NotFoundException("there is no blob $blobName")
        }
    }

    @Throws(IOException::class)
    override fun getBlobString(id: String, blobName: String): String {
        return readonly {
            val entity = getEntity(id, it)
            entity.getBlobString(blobName) ?: throw NotFoundException("there is no blob $blobName")
        }
    }

    private fun Entity.applyValues(property: EntityProperty) {
        property.value = safeTrim(property.value)
        val value = property.string2value()
        if (value != null) {
            this.setProperty(property.name, value)
        }
    }

    override fun updateEntity(id: String, vo: ChangeSummary): EntityView {
        transactional { t ->
            val entity = getEntity(id, t)
            vo.properties.forEach {
                val newValue = it.newValue
                if (newValue == null) {
                    entity.deleteProperty(it.name)
                } else {
                    entity.applyValues(newValue)
                }
            }

            vo.links.forEach {
                val newValue = it.newValue
                val oldValue = it.oldValue
                if (it.totallyRemoved) {
                    entity.deleteLinks(it.name)
                } else if (newValue == null) {
                    if (oldValue != null) {
                        val linked = getEntity(oldValue.id, t)
                        entity.deleteLink(it.name, linked)
                    }
                } else {
                    val linked = getEntity(newValue.id, t)
                    entity.addLink(it.name, linked)
                }
            }
            id
        }
        return getEntity(id)
    }

    override fun getEntity(id: String): EntityView {
        return transactional { t ->
            val entity = getEntity(id, t)
            entity.asView()
        }
    }

    override fun deleteEntity(id: String) {
        transactional {
            val entity = getEntity(id, it)
            entity.delete()
        }
    }

    override fun deleteEntitiesJob(typeId: Int, term: String?): Job {
        return object : EntityBulkJob(store) {

            override fun Entity.doAction() {
                delete()
            }

            override val affectedEntities: EntityIterable
                get() = transactional {
                    val type = store.getEntityType(typeId)
                    smartSearch(term, type, typeId, it)
                }

            override fun toString(): String {
                return "Bulk delete entities job for type $typeId and query '$term'"
            }
        }
    }

    private fun getEntity(id: String, txn: StoreTransaction): Entity {
        try {
            return txn.getEntity(txn.toEntityId(id))
        } catch (e: RuntimeException) {
            logger.error(e) { "entity not found by '$id'" }
            throw EntityNotFoundException(e, id)
        }

    }

    private fun safeTrim(value: String?): String? {
        return value
            ?.trim { it <= ' ' }
            ?.takeIf { it.isNotEmpty() }
    }

    private fun <T> transactional(call: (StoreTransaction) -> T): T {
        return store.computeInTransaction { call(it.asOStoreTransaction()) }
    }

    private fun <T> readonly(call: (StoreTransaction) -> T): T {
        return store.computeInReadonlyTransaction { call(it.asOStoreTransaction()) }
    }
}
