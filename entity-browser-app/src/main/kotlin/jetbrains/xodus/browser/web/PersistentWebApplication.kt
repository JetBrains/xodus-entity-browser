package jetbrains.xodus.browser.web

import jetbrains.xodus.browser.web.db.DatabaseService
import jetbrains.xodus.browser.web.db.EnvironmentStoreService
import jetbrains.xodus.browser.web.db.StoreService
import java.util.concurrent.ConcurrentHashMap

open class PersistentWebApplication(override val databaseService: DatabaseService) : WebApplication {

    override val allServices = ConcurrentHashMap<String, Services>()

    override fun start() {
        databaseService.all().forEach {
            val started = tryStartServices(it, true)
            databaseService.markStarted(it.uuid, started)
        }
    }

    override fun tryStartServices(db: DBSummary, silent: Boolean): Boolean {
        if (allServices.containsKey(db.uuid)) {
            return true
        }
        val service = tryToCreateStoreService(db, silent)
        service?.also {
            allServices[db.uuid] = Services(it)
            databaseService.markStarted(db.uuid, true)
        }
        return service != null
    }

    private fun tryToCreateStoreService(db: DBSummary, silent: Boolean): StoreService? {
        return try {
            EnvironmentStoreService(db)
        } catch (e: DatabaseException) {
            if (silent) {
                null
            } else {
                throw e
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun stop(db: DBSummary) {
        allServices[db.uuid]?.also {
            it.stop()
            databaseService.markStarted(db.uuid, false)
        }
        allServices.remove(db.uuid)
    }

    override fun stop() {
        databaseService.all().forEach { stop(it) }
    }

}