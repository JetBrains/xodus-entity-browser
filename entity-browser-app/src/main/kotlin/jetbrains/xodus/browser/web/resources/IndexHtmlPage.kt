package jetbrains.xodus.browser.web.resources

import io.ktor.server.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.server.response.respondText

open class IndexHtmlPage(val deploymentPath: String) {

    open val indexHtml by lazy {
        val inputStream = javaClass.getResourceAsStream("/entity/browser/static/index.html")
        val text = inputStream.reader().readText()
        if (deploymentPath.isBlank() || deploymentPath == "/") {
            text.postProcess()
        } else {
            text.replace("<base href=\"/\">", "<base href=\"$deploymentPath/\">").postProcess()
        }
    }

    open fun String.postProcess(): String {
        return this
    }

    suspend fun respondIndexHtml(call: ApplicationCall) {
        call.respondText(ContentType.Text.Html) {
            indexHtml
        }
    }

}

