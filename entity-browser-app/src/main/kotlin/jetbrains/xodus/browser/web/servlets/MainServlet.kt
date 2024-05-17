package jetbrains.xodus.browser.web.servlets

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainServlet: HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_OK
        response.writer.println("{ \"status\": \"I'm main servlet\"}")
    }
}