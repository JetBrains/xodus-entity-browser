package jetbrains.xodus.browser.web.resources


//import io.ktor.application.call
//import io.ktor.features.BadRequestException
//import io.ktor.features.NotFoundException
//import io.ktor.request.receive
//import io.ktor.response.respond
//import io.ktor.routing.Route
//import io.ktor.routing.get
//import io.ktor.routing.post
//import io.ktor.routing.route
//import jetbrains.xodus.browser.web.AppRoute
import jetbrains.xodus.browser.web.ApplicationSummary
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.WebApplication

//class DBs(webApp: WebApplication) : ResourceSupport(webApp), AppRoute {
//
//    override fun Route.install() {
//        route("/dbs") {
//            get {
//                call.respond(
//                        ApplicationSummary(
//                                isReadonly = webApp.isReadonly,
//                                dbs = webApp.databaseService.all().map { it.secureCopy() })
//                )
//            }
//            post {
//                val newSummary = call.receive(DBSummary::class)
//                if (webApp.isReadonly) {
//                    throw BadRequestException("application is R/O")
//                }
//                val summary = webApp.databaseService.add(newSummary)
//                if (newSummary.isOpened) {
//                    webApp.tryStartServices(summary, false)
//                }
//                call.respond(
//                        webApp.databaseService.find(summary.uuid)?.secureCopy() ?: throw NotFoundException()
//                )
//            }
//        }
//    }
//
//    private fun DBSummary.secureCopy(): DBSummary {
//        return copy(encryptionKey = null, encryptionIV = null, encryptionProvider = null)
//    }
//
//}