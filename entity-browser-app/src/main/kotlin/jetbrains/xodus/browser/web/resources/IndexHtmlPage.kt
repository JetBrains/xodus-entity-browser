package jetbrains.xodus.browser.web.resources

import jetbrains.xodus.browser.web.JsonTransformer
import jetbrains.xodus.browser.web.Resource
import jetbrains.xodus.browser.web.RestError
import spark.kotlin.Http

class IndexHtmlPage(private val context: String) : Resource {

    private val indexHtml by lazy {
        val inputStream = javaClass.getResourceAsStream("/static/index.html")
        inputStream.reader().readText().replace("<base href=\"/\">", "<base href=\"/$context/\">")
    }

    override fun registerRouting(http: Http) {
        http.notFound {
            if (!request.pathInfo().startsWith("/api")) {
                response.status(200)
                response.header("content-type", "text/html")
                indexHtml
            } else {
                response.status(404)
                JsonTransformer.render(RestError("${request.pathInfo()} - not found"))
            }
        }
    }
}

