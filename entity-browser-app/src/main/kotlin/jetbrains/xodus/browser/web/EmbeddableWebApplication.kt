package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.xodus.browser.web.db.EmbeddableDatabaseService
import jetbrains.xodus.browser.web.db.StoreService
import jetbrains.xodus.browser.web.db.asSummary

open class EmbeddableWebApplication(open val lookup: () -> List<PersistentEntityStoreImpl>) : WebApplication {

    override val databaseService = EmbeddableDatabaseService {
        lookup().map {
            val readonly = it.isForcedlyReadonly()
            it.asSummary(readonly)
        }
    }

    override val allServices: Map<String, Services>
        get() = lookup().associate { it.name to Services(StoreService(it, false)) }

    override fun start() {}

    override fun stop() {}

    override fun stop(db: DBSummary) {}

    override fun tryStartServices(db: DBSummary, silent: Boolean): Boolean {
        return false
    }

    open fun PersistentEntityStoreImpl.isForcedlyReadonly() = true

}