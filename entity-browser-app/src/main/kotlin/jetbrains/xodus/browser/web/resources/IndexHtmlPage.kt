package jetbrains.xodus.browser.web.resources

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText

open class IndexHtmlPage(val context: String) {

    open val indexHtml by lazy {
        val inputStream = javaClass.getResourceAsStream("/entity/browser/static/index.html")
        val text = inputStream.reader().readText()
        if (context.isBlank() || context == "/") {
            text.postProcess()
        } else {
            text.replace("<base href=\"/\">", "<base href=\"$context/\">").postProcess()
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

