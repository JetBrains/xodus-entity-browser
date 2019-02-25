package jetbrains.xodus.browser.web.resources

import io.ktor.application.ApplicationCall
import io.ktor.features.BadRequestException
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.NotFoundException
import jetbrains.xodus.browser.web.WebApplication
import jetbrains.xodus.browser.web.db.JobsService
import jetbrains.xodus.browser.web.db.StoreService
import jetbrains.xodus.browser.web.servicesOf

open class ResourceSupport(
        val webApp: WebApplication
) {

    val ApplicationCall.db: DBSummary
        get() {
            val uuid = parameters["uuid"] ?: throw NotFoundException("database not found")
            return webApp.databaseService.find(uuid) ?: throw NotFoundException("database not found")
        }

    val ApplicationCall.jobsService: JobsService
        get() {
            val uuid = parameters["uuid"] ?: throw NotFoundException("database not found")
            return webApp.servicesOf(uuid).jobsService
        }

    val ApplicationCall.storeService: StoreService
        get() {
            val uuid = parameters["uuid"] ?: throw NotFoundException("database service not found")
            return webApp.servicesOf(uuid).storeService
        }

    fun ApplicationCall.assertEditable() {
        val uuid = parameters["uuid"] ?: throw NotFoundException("database service not found")
        if (webApp.servicesOf(uuid).storeService.isReadonly) {
            throw BadRequestException("store is readonly")
        }
    }

}