package jetbrains.xodus.browser.web.resources

import io.ktor.application.ApplicationCall
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.NotFoundException
import jetbrains.xodus.browser.web.db.Databases
import jetbrains.xodus.browser.web.db.JobsService
import jetbrains.xodus.browser.web.db.StoreService
import jetbrains.xodus.browser.web.servicesOf

interface ResourceSupport {

    val ApplicationCall.db: DBSummary
        get() {
            val uuid = parameters["uuid"] ?: throw NotFoundException("database not found")
            return Databases.find(uuid)
        }

    val ApplicationCall.jobsService: JobsService
        get() {
            val uuid = parameters["uuid"] ?: throw NotFoundException("database not found")
            return servicesOf(uuid).jobsService
        }

    val ApplicationCall.storeService: StoreService
        get() {
            val uuid = parameters["uuid"] ?: throw NotFoundException("database service not found")
            return servicesOf(uuid).storeService
        }

}