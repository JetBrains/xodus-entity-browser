package jetbrains.xodus.browser.web.servlets

import java.io.*
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class StaticServlet : HttpServlet() {

    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val html = indexHtml(request)
        println("StaticServlet")
        response.contentType = "text/html"
        response.status = HttpServletResponse.SC_OK
        response.writer.println(html)

    }

    private fun indexHtml(request: HttpServletRequest): String{
        val deploymentPath = request.contextPath
        val inputStream = javaClass.getResourceAsStream("/entity/browser/static/index.html")
//        val inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/entity/browser/static/index.html")
        val text = inputStream.reader().readText()
        if (deploymentPath.isBlank() || deploymentPath == "/") {
            return text.postProcess()
        } else {
            return text.replace("<base href=\"/\">", "<base href=\"$deploymentPath/\">").postProcess()
        }
    }

    open fun String.postProcess(): String {
        return this
    }

}