package jetbrains.xodus.browser.web.resources

import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.db.Databases
import jetbrains.xodus.browser.web.db.JobsService
import jetbrains.xodus.browser.web.db.StoreService
import jetbrains.xodus.browser.web.servicesOf
import spark.kotlin.RouteHandler

interface ResourceSupport {

    val RouteHandler.db: DBSummary
        get() {
            val uuid = request.params("uuid")
            return Databases.find(uuid)
        }

    val RouteHandler.jobsService: JobsService
        get() {
            val uuid = request.params("uuid")
            return servicesOf(uuid).jobsService
        }

    val RouteHandler.storeService: StoreService
        get() {
            val uuid = request.params("uuid")
            return servicesOf(uuid).storeService
        }

}