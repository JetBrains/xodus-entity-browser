package jetbrains.xodus.browser.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jetbrains.xodus.browser.web.resources.DB
import jetbrains.xodus.browser.web.resources.DBs
import jetbrains.xodus.browser.web.resources.Entities
import jetbrains.xodus.browser.web.resources.IndexHtmlPage
import jetbrains.xodus.browser.web.search.SearchQueryException
import mu.KLogging
import spark.Response
import spark.ResponseTransformer
import spark.Spark.exception
import spark.kotlin.Http
import spark.kotlin.RouteHandler
import spark.kotlin.ignite
import java.net.HttpURLConnection


val mapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(KotlinModule())

object JsonTransformer : ResponseTransformer {

    override fun render(model: Any): String {
        return mapper.writeValueAsString(model)
    }
}

const val json = "application/json"

inline fun <reified T> Http.safePost(path: String = "", crossinline executor: RouteHandler.(T) -> Any) {
    post(path, json) {
        val t = mapper.readValue(request.body(), T::class.java)
        response.type(json)
        JsonTransformer.render(executor(t))
    }
}

fun Http.safePost(path: String = "", executor: RouteHandler.() -> Any) {
    post(path) {
        response.type(json)
        JsonTransformer.render(executor())
    }
}

inline fun <reified T> Http.safePut(path: String, crossinline executor: RouteHandler.(T) -> Any) {
    put(path, json) {
        val t = mapper.readValue(request.body(), T::class.java)
        response.type(json)
        JsonTransformer.render(executor(t))
    }
}


fun Http.safeGet(path: String = "", executor: RouteHandler.() -> Any) {
    get(path, json) {
        response.type(json)
        JsonTransformer.render(executor())
    }
}

fun Http.safeDelete(path: String = "", executor: RouteHandler.() -> Any) {
    delete(path, json) {
        response.type(json)
        JsonTransformer.render(executor())
    }
}

object HttpServer : KLogging() {

    private lateinit var http: Http

    private val resources = listOf(
            // rest api
            DBs(),
            DB(),
            Entities(),

            // index html
            IndexHtmlPage()
    )

    fun setup(port: Int) {
        http = ignite().port(port).apply {
            staticFiles.location("/static/")
            after {
                logger.info {
                    "'${request.requestMethod()} ${request.pathInfo()}' - ${response.status()} ${response.type() ?: ""}"
                }
            }
            resources.forEach { it.registerRouting(this) }

            exception(EntityNotFoundException::class.java) { e, _, response ->
                logger.error("getting entity failed", e)
                response.status(HttpURLConnection.HTTP_NOT_FOUND)
                withBody(response, e)
            }
            exception(InvalidFieldException::class.java) { e, _, response ->
                logger.error("error updating entity", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                withBody(response, e)
            }
            exception(DatabaseException::class.java) { e, _, response ->
                logger.error("error with working with database", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                withBody(response, e)
            }
            exception(SearchQueryException::class.java) { e, request, response ->
                logger.warn("error executing '${request.requestMethod()}' request for '${request.pathInfo()}'", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                withBody(response, e)
            }
            exception(NumberFormatException::class.java) { e, _, response ->
                logger.debug("error parsing request path or query parameter", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                response.body(JsonTransformer.render(e.toVO()))
            }
            exception(NotFoundException::class.java) { e, _, response ->
                logger.debug("can't handle database", e)
                response.status(HttpURLConnection.HTTP_NOT_FOUND)
                withBody(response, e)
            }
            exception(Exception::class.java) { e, _, response ->
                logger.error("unexpected exception", e)
                response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
                withBody(response, e)
            }

            internalServerError {
                "Sorry, something went wrong. Check server logs"
            }
        }
    }

    private fun withBody(response: Response, e: Exception) {
        val vo = if (e is WithMessage) e.toVO() else e.toVO()
        response.body(JsonTransformer.render(vo))
    }

    fun stop() {
        http.stop()
    }
}

interface Resource {

    companion object : KLogging()

    fun registerRouting(http: Http)
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