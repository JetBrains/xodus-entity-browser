package com.lehvolk.xodus.web.resources

import spark.kotlin.Http

class IndexHtmlPage : com.lehvolk.xodus.web.Resource {

    private val indexHtml by lazy {
        val inputStream = javaClass.getResourceAsStream("/static/index.html")
        inputStream.reader().readText()
    }

    override fun registerRouting(http: Http) {
        http.notFound {
            if (!request.pathInfo().startsWith("/api")) {
                response.status(200)
                response.header("content-type", "text/html")
                indexHtml
            }
        }
    }
}

