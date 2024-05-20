package jetbrains.xodus.browser.web.servlets

import java.io.*
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainServlet : HttpServlet() {


    /**
     * This method handles the HTTP request and generates the appropriate response based on the requested path.
     *
     * @param request The HttpServletRequest object representing the incoming request.
     * @param response The HttpServletResponse object representing the response to be sent back to the client.
     * @throws ServletException If any servlet-specific error occurs.
     * @throws IOException If any I/O error occurs.
     */
    @Throws(ServletException::class, IOException::class)
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        val path = request.servletPath
        with(path) {
            when {
//                /api/dbs/{uuid}/entities
//                /api/dbs/{uuid}
//                /api/dbs
                startsWith("/api/dbs") -> {}
                (equals("/") || startsWith("/databases") ) && request.method == "GET" -> {

                    response.contentType = "text/html"
                    val context: ServletContext = servletContext
                    val inputStream: InputStream = context.getResourceAsStream("/WEB-INF/index.html")
                    val isr = InputStreamReader(inputStream)
                    val reader = BufferedReader(isr)
                    val writer: PrintWriter = response.writer
                    var text: String?
                    while ((reader.readLine().also { text = it }) != null) {
                        writer.println(text)
                    }
                }
                else -> {
                    response.contentType = "application/json"
                    response.status = HttpServletResponse.SC_NOT_FOUND
                    response.writer.println("{ \"status\": \"failed\", \"message\":\"Page not found\"}")
                }
            }
        }
    }


}

object resources {

}