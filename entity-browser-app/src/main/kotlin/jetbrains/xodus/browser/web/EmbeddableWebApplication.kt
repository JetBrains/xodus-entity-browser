package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.xodus.browser.web.db.EmbeddableDatabaseService
import jetbrains.xodus.browser.web.db.StoreService
import jetbrains.xodus.browser.web.db.asSummary

open class EmbeddableWebApplication(open val lookup: () -> List<PersistentEntityStore>) : WebApplication {

    override val databaseService = EmbeddableDatabaseService {
        lookup().map { persistentStore ->
            val readonly = persistentStore.isForcedlyReadonly()
            persistentStore.asSummary(readonly)
        }
    }

    override val allServices: Map<String, Services>
        get() = lookup().associate { persistentStore ->
            persistentStore.name to Services(StoreService(persistentStore, false))
        }

    override fun start() {}

    override fun stop() {}

    override fun stop(db: DBSummary) {}

    override fun tryStartServices(db: DBSummary, silent: Boolean): Boolean {
        return false
    }

    open fun PersistentEntityStore.isForcedlyReadonly() = true

}