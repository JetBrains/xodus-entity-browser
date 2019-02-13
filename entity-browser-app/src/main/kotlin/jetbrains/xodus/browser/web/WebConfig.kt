package jetbrains.xodus.browser.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import jetbrains.xodus.browser.web.resources.DB
import jetbrains.xodus.browser.web.resources.DBs
import jetbrains.xodus.browser.web.resources.Entities
import jetbrains.xodus.browser.web.resources.IndexHtmlPage
import jetbrains.xodus.browser.web.search.SearchQueryException
import mu.KLogging
import org.slf4j.event.Level


lateinit var mapper: ObjectMapper

open class HttpServer(val context: String = "/") : KLogging() {

    internal val indexHtml = IndexHtmlPage(context)

    private val resources = listOf(
            // rest api
            DBs(),
            DB(),
            Entities()
    )

    fun setup(application: Application, port: Int) {
        with(application) {
//            install(HttpsRedirect) {
//                sslPort = port
//            }
            install(DefaultHeaders)
            install(Compression)

            install(ContentNegotiation) {
                jackson {
                    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    mapper = this
                }
            }

            install(CallLogging) {
                install(CallLogging) {
                    level = Level.DEBUG
                    filter { call -> call.request.path().startsWith("/$context/api") }
                }
            }
            installStatusPages()
            installStatic()

            installIndexHTML()
            installRestApi()

        }
    }

    open fun Application.installStatic() {
        routing {
            static(context) {
                files("static")
            }
        }
    }

    open fun Application.installIndexHTML() {
        routing {
            static {
                files("static")
            }
        }
    }

    open fun Application.installRestApi() {
        routing {
            route("$context/api") {
                resources.forEach { with(it) { install() } }
            }
        }
    }

    private fun Application.installStatusPages() {
        install(StatusPages) {
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
                logger.debug("error parsing request path or query parameter", it)
                call.respond(HttpStatusCode.BadRequest, it)
            }
            exception<NotFoundException> {
                logger.debug("can't handle database", it)
                call.respond(HttpStatusCode.NotFound, it)
            }
            exception<Exception> {
                logger.error("unexpected exception", it)
                call.respond(HttpStatusCode.InternalServerError, it)
            }
            exception<io.ktor.features.NotFoundException> {
                logger.error("unexpected exception", it)
                if (!call.request.path().startsWith("/$context/api")) {
                    with(indexHtml) { call.respondIndexHtml() }
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