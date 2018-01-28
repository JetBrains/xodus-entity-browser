package com.lehvolk.xodus.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.lehvolk.xodus.web.resources.DB
import com.lehvolk.xodus.web.resources.DBs
import com.lehvolk.xodus.web.resources.Entities
import com.lehvolk.xodus.web.search.SearchQueryException
import mu.KLogging
import spark.ResponseTransformer
import spark.Spark.exception
import spark.kotlin.Http
import spark.kotlin.RouteHandler
import spark.kotlin.ignite
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


val mapper: ObjectMapper = ObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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

interface Resource {

    companion object : KLogging()

    fun registerRouting(http: Http)
}

fun main(args: Array<String>) {
    val port = Integer.getInteger("server.port", 18080)
    Application.start()
    OS.launchBrowser(port)

    exception(EntityNotFoundException::class.java) { e, _, response ->
        Resource.logger.error("getting entity failed", e)
        response.status(HTTP_NOT_FOUND)
    }
    exception(InvalidFieldException::class.java) { e, _, response ->
        Resource.logger.error("error updating entity", e)
        response.status(HTTP_BAD_REQUEST)
    }
    exception(SearchQueryException::class.java) { e, request, response ->
        Resource.logger.warn("error executing '${request.requestMethod()}' request for '${request.pathInfo()}'", e)
        response.status(HTTP_BAD_REQUEST)
    }
    exception(NumberFormatException::class.java) { e, _, response ->
        Resource.logger.debug("error parsing request path or query parameter", e)
        response.status(HTTP_BAD_REQUEST)
    }

    val resources = listOf(
            DBs(),
            DB(),
            Entities()
    )

    val http = ignite().port(port).apply {
        resources.forEach { it.registerRouting(this) }
    }
    http.staticFiles.location("/META-INF/resources")


}

@WebServlet(urlPatterns = ["/databases/*"], loadOnStartup = 1)
class IndexHtml : HttpServlet() {

    private val indexHtml by lazy {
        val inputStream = servletConfig.servletContext.getResourceAsStream("/index.html")
        inputStream.reader().readText()
    }

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        resp!!.writer.write(indexHtml)
    }

}

private object OS : KLogging() {

    fun launchBrowser(port: Int) {
        val url = "http://$hostName:$port"
        logger.info("try to open browser for '{}'", url);
        try {
            val osName = "os.name".system()
            if (osName.startsWith("Mac OS")) {
                logger.info("mac os detected");
                val fileMgr = Class.forName("com.apple.eio.FileManager")
                val openURL = fileMgr.getDeclaredMethod("openURL", String::class.java)
                openURL.invoke(null, url)
            } else if (osName.startsWith("Windows")) {
                logger.info("windows detected");
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url)
            } else {
                // Unix or Linux
                logger.info("linux detected");
                val browsers = arrayOf("google-chrome", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape")
                val selectedBrowser: String? = browsers.firstOrNull { Runtime.getRuntime().exec(arrayOf("which", it)).waitFor() == 0 }
                if (selectedBrowser == null) {
                    throw Exception("Couldn't find web browser")
                } else {
                    logger.info("open url using browser {}", selectedBrowser);
                    Runtime.getRuntime().exec(arrayOf(selectedBrowser, url))
                }
            }
        } catch (e: Exception) {
            println("Unable to open browser: " + e.message)
        }
    }

    private val hostName: String
        get() {
            try {
                return InputStreamReader(Runtime.getRuntime().exec("hostname").inputStream).readLines().first()
            } catch (ignored: IOException) {
            }
            return "localhost"
        }
}


interface WithMessage {
    val msg: String
}

class EntityNotFoundException(cause: Throwable? = null, val typeId: Int, val entityId: Long) : RuntimeException(cause), WithMessage {

    override val msg: String = "Error getting entity by type '$typeId' and id='$entityId'"
}

class InvalidFieldException(cause: Throwable, val fieldName: String, val fieldValue: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "invalid value of property '$fieldName': '$fieldValue'"
}
