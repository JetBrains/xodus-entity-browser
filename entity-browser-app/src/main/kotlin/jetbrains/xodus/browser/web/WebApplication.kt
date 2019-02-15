package jetbrains.xodus.browser.web


import jetbrains.xodus.browser.web.db.DatabaseService
import jetbrains.xodus.browser.web.db.JobsService
import jetbrains.xodus.browser.web.db.StoreService


class Services(val storeService: StoreService,
               val jobsService: JobsService = JobsService()) {

    fun stop() {
        jobsService.stop()
        storeService.stop()
    }

}

interface WebApplication {
    val databaseService: DatabaseService
    val allServices: Map<String, Services>

    fun start()
    fun stop()

    fun stop(db: DBSummary)
    fun tryStartServices(db: DBSummary, silent: Boolean = true): Boolean

    val isReadonly get() = databaseService.isReadonly
}

fun WebApplication.servicesOf(dbUUID: String): Services = allServices[dbUUID]
        ?: throw NotFoundException("no database founded for $dbUUID")

fun String.system(): String = System.getProperty(this)


