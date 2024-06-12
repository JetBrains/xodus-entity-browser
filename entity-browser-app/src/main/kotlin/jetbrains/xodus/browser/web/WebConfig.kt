package jetbrains.xodus.browser.web

import io.ktor.server.application.*
import io.ktor.server.application.call
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.compression.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jetbrains.xodus.browser.web.resources.DB
import jetbrains.xodus.browser.web.resources.DBs
import jetbrains.xodus.browser.web.resources.Entities
import jetbrains.xodus.browser.web.resources.IndexHtmlPage
import jetbrains.xodus.browser.web.search.SearchQueryException
import mu.KLogging


open class HttpServer(webApplication: WebApplication, val appContext: String = "/") : KLogging() {

    open val indexHtml = IndexHtmlPage(appContext)

    open val resources = listOf<AppRoute>(
            // rest api
            DBs(webApplication),
            DB(webApplication),
            Entities(webApplication)
    )

    fun setup(application: Application) {
        with(application) {
            install(Compression)

            install(ContentNegotiation) {
                gson { }
            }

            installStatusPages()
            installStatic()

            installIndexHTML()
            installRestApi()

            installAdditionalFeatures()
        }
    }

    open fun Application.installAdditionalFeatures() {
    }

    open fun Application.installStatic() {
        routing {
            static(appContext) {
                resources("entity.browser.static")
            }
        }
    }

    private fun Routing.installIndexHtml(url: String) {
        get("$appContext$url") {
            indexHtml.respondIndexHtml(call)
        }
    }

    open fun Application.installIndexHTML() {
        routing {
            //damn ktor StatusPages is not working in war
            listOf("", "/", "/databases/{...}", "/databases").forEach {
                installIndexHtml(it)
            }
        }
    }

    open fun Application.installRestApi() {
        routing {
            route(appContext) {
                route("/api") {
                    resources.forEach { with(it) { install() } }
                }
            }
        }
    }

    private fun Application.installStatusPages() {
        install(StatusPages) {
            status(HttpStatusCode.NotFound) {call, _->
                if (!call.request.path().startsWith("$appContext/api")) {
                    indexHtml.respondIndexHtml(call)
                }
            }
            exception<EntityNotFoundException>{ call, cause ->
                logger.error("getting entity failed", cause)
                call.respond(HttpStatusCode.NotFound, cause)
            }
            exception<InvalidFieldException> { call, cause ->
                logger.error("error updating entity", cause)
                call.respond(HttpStatusCode.BadRequest, cause)
            }
            exception<DatabaseException> { call, cause ->
                logger.error("error with working with database", cause)
                call.respond(HttpStatusCode.BadRequest, cause)
            }
            exception<SearchQueryException> { call, cause ->
                logger.warn("error executing '${call.request.httpMethod.value}' request for '${call.request.path()}'", cause)
                call.respond(HttpStatusCode.BadRequest, cause)
            }
            exception<NumberFormatException> { call, cause ->
                logger.info("error parsing request path or query parameter", cause)
                call.respond(HttpStatusCode.BadRequest, cause)
            }
            exception<NotFoundException> { call, cause ->
                logger.info("can't handle database", cause)
                call.respond(HttpStatusCode.NotFound, cause)
            }
            exception<Exception> { call, cause ->
                logger.error("unexpected exception", cause)
                call.respond(HttpStatusCode.InternalServerError, cause)
            }

            exception<io.ktor.server.plugins.NotFoundException> { call, cause ->
                logger.error("unexpected exception", cause)
                if (!call.request.path().startsWith("$appContext/api")) {
                    indexHtml.respondIndexHtml(call)
                } else {
                    call.respond(HttpStatusCode.NotFound, cause)
                }
            }
        }
    }

    private suspend fun ApplicationCall.respond(status: HttpStatusCode, e: Exception) {
        val vo = if (e is WithMessage) e.toVO() else e.toVO()
        respond(status, vo)
    }
}

interface AppRoute {

    companion object : KLogging()

    fun Route.install()
}

interface WithMessage {
    val msg: String

    fun toVO() = RestError(msg)
}

class EntityNotFoundException(cause: Throwable? = null, id: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "Error getting entity by type '$id'"
}

class InvalidFieldException(cause: Throwable, fieldName: String, fieldValue: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "invalid value of property '$fieldName': '$fieldValue'"
}

class NotFoundException(override val msg: String) : RuntimeException(), WithMessage
class DatabaseException(override val msg: String) : RuntimeException(), WithMessage

data class RestError(val errorMessage: String)

fun Exception.toVO() = RestError(message ?: "unknown error")