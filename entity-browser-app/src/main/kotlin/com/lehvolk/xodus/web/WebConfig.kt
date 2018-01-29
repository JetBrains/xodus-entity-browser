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
import spark.Spark.staticFileLocation
import spark.kotlin.Http
import spark.kotlin.RouteHandler
import spark.kotlin.ignite
import java.net.HttpURLConnection


val mapper: ObjectMapper = ObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    registerModule(KotlinModule())
}

object JsonTransformer : ResponseTransformer {

    override fun render(model: Any): String {
        return mapper.writeValueAsString(model)
    }
}


inline fun <reified T> Http.safePost(path: String, crossinline executor: RouteHandler.(T) -> Any) {
    post(path, "application/json") {
        val t = mapper.readValue(request.body(), T::class.java)
        JsonTransformer.render(executor(t))
    }
}

inline fun <reified T> Http.safePut(path: String, crossinline executor: RouteHandler.(T) -> Any) {
    put(path, "application/json", JsonTransformer) {
        val t = mapper.readValue(request.body(), T::class.java)
        JsonTransformer.render(executor(t))
    }
}


fun Http.safeGet(path: String, executor: RouteHandler.() -> Any) {
    get(path, "application/json") {
        JsonTransformer.render(executor())
    }
}

fun Http.safeDelete(path: String, executor: RouteHandler.() -> Any) {
    delete(path, "application/json") {
        JsonTransformer.render(executor())
    }
}

object HttpServer : KLogging() {

    fun setup(port: Int) {

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

        val resources = listOf(
                // rest api
                DBs(),
                DB(),
                Entities(),

                // index html
                IndexHtmlPage()
        )

        ignite().port(port).apply {
            staticFiles.location("/static/")
            resources.forEach { it.registerRouting(this) }
        }
    }
}

interface Resource {

    companion object : KLogging()

    fun registerRouting(http: Http)
}

interface WithMessage {
    val msg: String
}

class EntityNotFoundException(cause: Throwable? = null, typeId: Int, entityId: Long) : RuntimeException(cause), WithMessage {

    override val msg: String = "Error getting entity by type '$typeId' and id='$entityId'"
}

class InvalidFieldException(cause: Throwable, fieldName: String, fieldValue: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "invalid value of property '$fieldName': '$fieldValue'"
}
