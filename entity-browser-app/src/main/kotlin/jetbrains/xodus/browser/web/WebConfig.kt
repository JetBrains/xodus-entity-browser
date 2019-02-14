package jetbrains.xodus.browser.web

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import jetbrains.xodus.browser.web.resources.DB
import jetbrains.xodus.browser.web.resources.DBs
import jetbrains.xodus.browser.web.resources.Entities
import jetbrains.xodus.browser.web.resources.IndexHtmlPage
import jetbrains.xodus.browser.web.search.SearchQueryException
import mu.KLogging
import org.slf4j.event.Level


//lateinit var mapper: ObjectMapper

open class HttpServer(val appContext: String = "/") : KLogging() {

    open val indexHtml = IndexHtmlPage(appContext)

    private val resources = listOf(
            // rest api
            DBs(),
            DB(),
            Entities()
    )

    fun setup(application: Application) {
        with(application) {
            install(DefaultHeaders)
            install(Compression)

            install(ContentNegotiation) {
                gson {
//                    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//                    mapper = this
                }
            }

            install(CallLogging) {
                level = Level.DEBUG
                filter { call -> call.request.path().startsWith("/$appContext/api") }
            }
            installStatusPages()
            installStatic()

            installIndexHTML()
            installRestApi()

        }
    }

    open fun Application.installStatic() {
        routing {
            static(appContext) {
                resources("static")
            }
        }
    }

    open fun Application.installIndexHTML() {
        routing {
            get(appContext) {
                indexHtml.respondIndexHtml(call)
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
            status(HttpStatusCode.NotFound) {
                val prefix = if(appContext.length > 1) "$appContext/api" else "/api"
                if (!call.request.path().startsWith(prefix)) {
                    indexHtml.respondIndexHtml(call)
                }
            }

            exception<EntityNotFoundException> {
                logger.error("getting entity failed", it)
                call.respond(HttpStatusCode.NotFound, it)
            }
            exception<InvalidFieldException> {
                logger.error("error updating entity", it)
                call.respond(HttpStatusCode.BadRequest, it)
            }
            exception<DatabaseException> {
                logger.error("error with working with database", it)
                call.respond(HttpStatusCode.BadRequest, it)
            }
            exception<SearchQueryException> {
                logger.warn("error executing '${call.request.httpMethod.value}' request for '${call.request.path()}'", it)
                call.respond(HttpStatusCode.BadRequest, it)
            }
            exception<NumberFormatException> {
                logger.info("error parsing request path or query parameter", it)
                call.respond(HttpStatusCode.BadRequest, it)
            }
            exception<NotFoundException> {
                logger.info("can't handle database", it)
                call.respond(HttpStatusCode.NotFound, it)
            }
            exception<Exception> {
                logger.error("unexpected exception", it)
                call.respond(HttpStatusCode.InternalServerError, it)
            }
            exception<io.ktor.features.NotFoundException> {
                logger.error("unexpected exception", it)
                if (!call.request.path().startsWith("/$context/api")) {
                    indexHtml.respondIndexHtml(call)
                } else {
                    call.respond(HttpStatusCode.NotFound, it)
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