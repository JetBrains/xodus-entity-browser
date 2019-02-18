package jetbrains.xodus.browser.web.resources

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText

class IndexHtmlPage(val context: String) {

    private val indexHtml by lazy {
        val inputStream = javaClass.getResourceAsStream("/entity/browser/static/index.html")
        val text = inputStream.reader().readText()
        if (context.isBlank() || context == "/") {
            text
        } else {
            text.replace("<base href=\"/\">", "<base href=\"$context/\">")
        }
    }

    suspend fun respondIndexHtml(call: ApplicationCall) {
        call.respondText(ContentType.Text.Html) {
            indexHtml
        }
    }

}

