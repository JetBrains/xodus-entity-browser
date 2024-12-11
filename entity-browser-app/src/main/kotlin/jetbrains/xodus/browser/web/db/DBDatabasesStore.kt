package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.entitystore.PersistentStoreTransaction
import jetbrains.exodus.env.EnvironmentConfig
import jetbrains.exodus.env.Environments
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.Home
import jetbrains.xodus.browser.web.NotFoundException
import mu.KLogging
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class DBDatabasesStore : DatabasesStore {

    companion object : KLogging()

    private val iv: Long = System.getProperty("xodus.entity.browser.env.iv", "0").toLong()
    private val key: String? = System.getProperty("xodus.entity.browser.env.key")

    private val location: String
        get() {
            return System.getProperty("xodus.entity.browser.db.store") ?: Home.dbHome.absolutePath
        }

    private val isEncrypted: Boolean get() = key != null
    private val dbType: String = "DB"

    private lateinit var store: PersistentEntityStoreImpl

    override fun start() {
        val config = EnvironmentConfig().also {
            if (isEncrypted) {
                it.cipherBasicIV = iv
                it.setCipherKey(key)
            }
        }
        try {
            logger.info { "Opening database on '$location'" }
            val env = Environments.newInstance(location, config)
            store = PersistentEntityStores.newInstance(env, "xodus-entity-browser")
            store.executeInTransaction {
                it as PersistentStoreTransaction
                store.getEntityTypeId(it, dbType, true)

                // Validate store
                it.getAll(dbType).toList().forEach { entity ->
                    try {
                        DBEntity(entity).summary()
                    } catch (e: Exception) {
                        entity.delete()
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Can't open database on '$location'", e)
        }
    }

    override fun add(dbSummary: DBSummary): DBSummary {
        val id = store.computeInTransaction {
            val entity = it.newEntity(dbType)
            val dbEntity = DBEntity(entity).merge(dbSummary).also {
                it.isOpened = false
            }
            dbEntity.id
        }
        return find(id) {
            throw NotFoundException("Database on '${dbSummary.location}' is already registered")
        }
    }

    override fun update(uuid: String, summary: DBSummary): DBSummary {
        store.transactional {
            DBEntity(it.getEntity(it.toEntityId(uuid))).merge(summary)
        }
        return find(uuid) {
            throw NotFoundException("Database on '$location' can't be modified")
        }
    }

    override fun delete(uuid: String) {
        store.transactional {
            it.getEntity(it.toEntityId(uuid)).delete()
        }
    }

    override fun all(): List<DBSummary> {
        return listDBs().toList()
    }

    override fun stop() {
        return store.close()
    }

    override fun find(uuid: String, error: () -> Nothing) = listDBs().firstOrNull { it.uuid == uuid } ?: error()

    private fun listDBs(): List<DBSummary> {
        return store.computeInTransaction {
            it.getAll(dbType).map { entity ->
                DBEntity(entity).summary()
            }
        }
    }

    class DBEntity(val entity: Entity) {
        val id = entity.toIdString()
        var location by requiredString()
        var key by string()

        var isOpened by boolean()
        var isReadonly by boolean()
        var isWatchReadonly by boolean()

        var isEncrypted by boolean()
        var encryptionKey by string()
        var encryptionIV by string()

        private fun boolean(): ReadWriteProperty<DBEntity, Boolean> {
            return object : ReadWriteProperty<DBEntity, Boolean> {

                override fun getValue(thisRef: DBEntity, property: KProperty<*>): Boolean {
                    return thisRef.entity.getProperty(property.name) as? Boolean ?: false
                }

                override fun setValue(thisRef: DBEntity, property: KProperty<*>, value: Boolean) {
                    thisRef.entity.setProperty(property.name, value)
                }
            }
        }

        private fun string(): ReadWriteProperty<DBEntity, String?> {
            return object : ReadWriteProperty<DBEntity, String?> {

                override fun getValue(thisRef: DBEntity, property: KProperty<*>): String? {
                    return thisRef.entity.getProperty(property.name) as? String
                }

                override fun setValue(thisRef: DBEntity, property: KProperty<*>, value: String?) {
                    if (value != null) {
                        thisRef.entity.setProperty(property.name, value)
                    } else {
                        thisRef.entity.deleteProperty(property.name)
                    }
                }
            }
        }

        private fun requiredString(): ReadWriteProperty<DBEntity, String> {
            return object : ReadWriteProperty<DBEntity, String> {

                override fun getValue(thisRef: DBEntity, property: KProperty<*>): String {
                    return thisRef.entity.getProperty(property.name) as String
                }

                override fun setValue(thisRef: DBEntity, property: KProperty<*>, value: String) {
                    thisRef.entity.setProperty(property.name, value)
                }
            }
        }

        fun summary() = DBSummary(
            uuid = id,
            location = location,
            key = key,
            isOpened = isOpened,
            isReadonly = isReadonly,
            isWatchReadonly = isWatchReadonly,
            isEncrypted = isEncrypted,
            encryptionIV = encryptionIV,
            encryptionKey = encryptionKey
        )

        fun merge(dbSummary: DBSummary) = apply {
            location = dbSummary.location
            key = dbSummary.key

            encryptionIV = dbSummary.encryptionIV
            encryptionKey = dbSummary.encryptionKey

            isOpened = dbSummary.isOpened
            isEncrypted = dbSummary.isEncrypted
            isReadonly = dbSummary.isReadonly
            isWatchReadonly = dbSummary.isWatchReadonly
        }
    }
}


