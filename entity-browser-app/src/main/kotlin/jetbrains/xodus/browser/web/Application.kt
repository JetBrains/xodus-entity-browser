package jetbrains.xodus.browser.web

import jetbrains.xodus.browser.web.db.DatabaseService
import jetbrains.xodus.browser.web.db.Databases
import jetbrains.xodus.browser.web.db.JobsService
import jetbrains.xodus.browser.web.db.StoreService
import java.util.concurrent.ConcurrentHashMap


class Services(val storeService: StoreService,
               val jobsService: JobsService = JobsService()) {

    fun stop() {
        jobsService.stop()
        storeService.stop()
    }

}

object Application {

    internal val allServices = ConcurrentHashMap<String, Services>()

    private val databaseService: DatabaseService = DatabaseService()

    fun start() {
        Databases.start()
        Databases.all().forEach { databaseService.tryStart(it.uuid) }
    }

    fun tryStartServices(db: DBSummary, silent: Boolean = true): Boolean {
        if (allServices.containsKey(db.uuid)) {
            return true
        }
        val service = try {
            StoreService(db)
        } catch (e: DatabaseException) {
            if (silent) {
                null
            } else {
                throw e
            }
        } catch (e: Exception) {
            null
        }
        service?.let {
            allServices[db.uuid] = Services(it)
        }
        return service != null
    }

    fun stop(db: DBSummary) {
        allServices[db.uuid]?.also {
            it.stop()
        }
        allServices.remove(db.uuid)
    }

}


fun servicesOf(dbUUID: String): Services = Application.allServices[dbUUID] ?: throw NotFoundException("no database founded for $dbUUID")

fun String.system(): String = System.getProperty(this)



