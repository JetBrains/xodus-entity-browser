package jetbrains.xodus.browser.web.resources

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//import io.ktor.application.ApplicationCall
//import io.ktor.http.ContentType
//import io.ktor.response.respondText

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

//    @Throws(ServletException::class, IOException::class)
//    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
//        response.contentType = "text/html"
//        response.status = HttpServletResponse.SC_OK
//        response.writer.println(indexHtml)
//    }
//
//    suspend fun respondIndexHtml(call: ApplicationCall) {
//        call.respondText(ContentType.Text.Html) {
//            indexHtml
//        }
//    }
//
}

