package jetbrains.xodus.browser.web.db


import jetbrains.exodus.crypto.InvalidCipherParametersException
import jetbrains.exodus.entitystore.*
import jetbrains.exodus.env.EnvironmentConfig
import jetbrains.exodus.env.Environments
import jetbrains.exodus.io.WatchingFileDataReaderWriterProvider
import jetbrains.xodus.browser.web.*
import jetbrains.xodus.browser.web.search.smartSearch
import mu.KLogging
import java.io.IOException
import java.io.InputStream

class StoreService {

    companion object : KLogging()

    private val store: PersistentEntityStoreImpl
    val isReadonly: Boolean

    constructor(store: PersistentEntityStoreImpl, isReadonly: Boolean) {
        this.store = store
        this.isReadonly = isReadonly
    }

    constructor(dbSummary: DBSummary) {
        try {
            val config = EnvironmentConfig().also {
                it.envIsReadonly = dbSummary.isReadonly
                if (dbSummary.isWatchReadonly && dbSummary.isReadonly) {
                    it.logDataReaderWriterProvider = WatchingFileDataReaderWriterProvider::class.java.name
                }
                if (dbSummary.isEncrypted) {
                    val initialization = try {
                        dbSummary.encryptionIV?.toLong()
                    } catch (e: Exception) {
                        throw InvalidCipherParametersException()
                    }
                    it.cipherBasicIV = initialization ?: throw InvalidCipherParametersException()
                    it.setCipherKey(dbSummary.encryptionKey)
                    it.cipherId = dbSummary.encryptionProvider?.cipherId ?: throw InvalidCipherParametersException()
                }
            }
            val environment = Environments.newInstance(dbSummary.location, config)
            store = dbSummary.key.let {
                if (it == null) {
                    PersistentEntityStores.newInstance(environment)
                } else {
                    PersistentEntityStores.newInstance(environment, it)
                }
            }
            isReadonly = store.environment.environmentConfig.envIsReadonly
        } catch (e: InvalidCipherParametersException) {
            val msg = "It seems that store encrypted with another parameters"
            logger.error(e) { msg }
            throw DatabaseException("Database is ciphered with different/unknown cipher parameters")
        } catch (e: RuntimeException) {
            val msg = "Can't get valid Xodus entity store location and store key. Check the configuration"
            logger.error(e) { msg }
            throw IllegalStateException(msg, e)
        }
    }

    fun stop() {
        var proceed = true
        var count = 1
        while (proceed && count <= 10) {
            try {
                logger.info { "trying to close persistent store. attempt $count" }
                store.close()
                proceed = false
                logger.info("persistent store closed")
            } catch (e: RuntimeException) {
                logger.error(e) { "error closing persistent store" }
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

    fun searchType(typeId: Int, q: String?, offset: Int, pageSize: Int): SearchPager {
        return readonly { t ->
            val type = store.getEntityType(t, typeId)
            val result = smartSearch(q, type, typeId, t)
            val totalCount = result.size()
            val items = result.skip(offset).take(pageSize).map { it.asView() }
            SearchPager(items.toList(), totalCount)
        }
    }

    fun searchEntity(id: String, linkName: String, offset: Int, pageSize: Int): LinkPager {
        return readonly { t ->
            getEntity(id, t).linkView(linkName, offset, pageSize)
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
                    val link = getEntity(it.id, t)
                    entity.addLink(it.name, link)
                }
            }
            entity.id.toString()
        }
        return getEntity(entityId)
    }

    @Throws(IOException::class)
    fun getBlob(id: String, blobName: String): InputStream {
        val tx = store.beginReadonlyTransaction()
        try {
            val entity = getEntity(id, tx)
            return entity.getBlob(blobName) ?: throw NotFoundException("there is no blob $blobName")
        } finally {
            tx.commit()
        }
    }

    private fun PersistentEntity.applyValues(property: EntityProperty) {
        property.value = safeTrim(property.value)
        val value = property.string2value()
        if (value != null) {
            this.setProperty(property.name, value)
        }
    }

    fun updateEntity(id: String, vo: ChangeSummary): EntityView {
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
                        val linked = getEntityOrStub(oldValue.id, t)
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

    fun getEntity(id: String): EntityView {
        return transactional { t ->
            val entity: PersistentEntity = getEntity(id, t)
            entity.asView()
        }
    }

    fun deleteEntity(id: String) {
        transactional {
            val entity = getEntity(id, it)
            entity.delete()
        }
    }

    fun deleteEntitiesJob(typeId: Int, term: String?): Job {
        return object : EntityBulkJob(store) {

            override fun Entity.doAction() {
                delete()
            }

            override val affectedEntities: EntityIterable
                get() = store.transactional {
                    val type = store.getEntityType(typeId)
                    smartSearch(term, type, typeId, it)
                }

            override fun toString(): String {
                return "Bulk delete entities job for type $typeId and query '$term'"
            }
        }
    }

    private fun getEntity(id: String, t: PersistentStoreTransaction): PersistentEntity {
        try {
            return t.getEntity(PersistentEntityId.toEntityId(id))
        } catch (e: RuntimeException) {
            logger.error(e) { "entity not found by '$id'" }
            throw EntityNotFoundException(e, id)
        }

    }

    private fun getEntityOrStub(id: String, t: PersistentStoreTransaction): PersistentEntity {
        val entityId = PersistentEntityId.toEntityId(id)
        return try {
            t.getEntity(entityId)
        } catch (e: EntityRemovedInDatabaseException) {
            logger.info { "entity not found by '$id'. using stub" }
            PersistentEntity(store, entityId as PersistentEntityId)
        }
    }

    private fun safeTrim(value: String?): String? {
        if (value == null) {
            return null
        }
        val trimmed = value.trim { it <= ' ' }
        return if (trimmed.isEmpty()) null else trimmed
    }

    private fun <T> transactional(call: (PersistentStoreTransaction) -> T): T {
        return store.transactional(call)
    }

    private fun <T> readonly(call: (PersistentStoreTransaction) -> T): T {
        return store.readonly(call)
    }

}


fun <T> PersistentEntityStore.transactional(call: (PersistentStoreTransaction) -> T): T {
    return computeInTransaction { call(it as PersistentStoreTransaction) }
}

fun <T> PersistentEntityStore.readonly(call: (PersistentStoreTransaction) -> T): T {
    return computeInReadonlyTransaction { call(it as PersistentStoreTransaction) }
}
