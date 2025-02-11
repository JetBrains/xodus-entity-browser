package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.xodus.browser.web.db.EmbeddableDatabaseService
import jetbrains.xodus.browser.web.db.PersistentStoreService
import jetbrains.xodus.browser.web.db.asSummary

open class EmbeddableWebApplication(open val lookup: () -> List<PersistentEntityStore>) : WebApplication {

    override val databaseService = EmbeddableDatabaseService {
        lookup().map { store ->
            store.asSummary(forcedReadonly = store.isForcedlyReadonly())
        }
    }

    override val allServices: Map<String, Services>
        get() = lookup().associate { store ->
            store.name to Services(PersistentStoreService(store = store, isReadonly = false))
        }

    override fun start() {}

    override fun stop() {}

    override fun stop(db: DBSummary) {}

    override fun tryStartServices(db: DBSummary, silent: Boolean): Boolean {
        return false
    }

    open fun PersistentEntityStore.isForcedlyReadonly() = true

}