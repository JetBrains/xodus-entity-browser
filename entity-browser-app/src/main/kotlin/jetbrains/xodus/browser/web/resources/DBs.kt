package jetbrains.xodus.browser.web.resources


import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import jetbrains.xodus.browser.web.AppRoute
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.db.DatabaseService
import jetbrains.xodus.browser.web.db.Databases

class DBs : AppRoute {

    private val databaseService = DatabaseService()

    override fun Route.install() {
        route("/dbs") {
            get {
                call.respond(Databases.all().map { it.secureCopy() })
            }
            post {
                val newSummary = call.receive(DBSummary::class)
                call.respond(
                        databaseService.add(newSummary).secureCopy()
                )
            }
        }
    }

    private fun DBSummary.secureCopy(): DBSummary {
        return copy(encryptionKey = null, encryptionIV = null, encryptionProvider = null)
    }

}
