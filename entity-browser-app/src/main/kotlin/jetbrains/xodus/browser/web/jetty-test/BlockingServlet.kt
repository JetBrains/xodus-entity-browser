package jetbrains.xodus.browser.web.`jetty-test`

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException

class BlockingServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_OK
        response.writer.println("{ \"status\": \"ok\"}")
    }
}