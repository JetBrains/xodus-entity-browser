package com.lehvolk.xodus.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lehvolk.xodus.web.resources.DB
import com.lehvolk.xodus.web.resources.DBs
import com.lehvolk.xodus.web.resources.Entities
import com.lehvolk.xodus.web.resources.IndexHtmlPage
import com.lehvolk.xodus.web.search.SearchQueryException
import mu.KLogging
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
                    "'${request.requestMethod()} ${request.pathInfo()}' - ${response.status()} ${response.type()
                            ?: ""} \n ${response.body()}"
                }
            }
            resources.forEach { it.registerRouting(this) }

            exception(EntityNotFoundException::class.java) { e, _, response ->
                logger.error("getting entity failed", e)
                response.status(HttpURLConnection.HTTP_NOT_FOUND)
            }
            exception(InvalidFieldException::class.java) { e, _, response ->
                logger.error("error updating entity", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
            }
            exception(SearchQueryException::class.java) { e, request, response ->
                logger.warn("error executing '${request.requestMethod()}' request for '${request.pathInfo()}'", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
            }
            exception(NumberFormatException::class.java) { e, _, response ->
                logger.debug("error parsing request path or query parameter", e)
                response.status(HttpURLConnection.HTTP_BAD_REQUEST)
            }
            exception(Exception::class.java) { e, _, _ ->
                logger.error("unexpected exception", e)
            }

            internalServerError {
                "Sorry, something went wrong. Check server logs"
            }
        }
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
}

class EntityNotFoundException(cause: Throwable? = null, id: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "Error getting entity by type '$id'"
}

class InvalidFieldException(cause: Throwable, fieldName: String, fieldValue: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "invalid value of property '$fieldName': '$fieldValue'"
}
