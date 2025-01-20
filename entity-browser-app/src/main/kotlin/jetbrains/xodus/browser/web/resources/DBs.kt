package jetbrains.xodus.browser.web.resources

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jetbrains.xodus.browser.web.AppRoute
import jetbrains.xodus.browser.web.ApplicationSummary
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.WebApplication

class DBs(webApp: WebApplication) : ResourceSupport(webApp), AppRoute {

    override fun Route.install() {
        route("/dbs") {
            get {
                call.respond(
                    ApplicationSummary(
                        isReadonly = webApp.isReadonly,
                        dbs = webApp.databaseService.all().map { it.secureCopy() })
                )
            }
            post {
                val newSummary = call.receive(DBSummary::class)
                if (webApp.isReadonly) {
                    throw BadRequestException("application is R/O")
                }
                val summary = webApp.databaseService.add(newSummary)
                if (newSummary.isOpened) {
                    webApp.tryStartServices(db = summary, silent = false)
                }
                call.respond(
                    webApp.databaseService.find(summary.uuid)?.secureCopy() ?: throw NotFoundException()
                )
            }
        }
    }

    private fun DBSummary.secureCopy(): DBSummary {
        return copy(encryptionKey = null, encryptionIV = null)
    }

}