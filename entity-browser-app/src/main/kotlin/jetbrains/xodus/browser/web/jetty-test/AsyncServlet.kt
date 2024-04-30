package jetbrains.xodus.browser.web.`jetty-test`

import javax.servlet.ServletException
import javax.servlet.WriteListener
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class AsyncServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val content = ByteBuffer.wrap(HEAVY_RESOURCE.toByteArray(StandardCharsets.UTF_8))

        val async = request.startAsync()
        val out = response.outputStream
        out.setWriteListener(object : WriteListener {
            @Throws(IOException::class)
            override fun onWritePossible() {
                while (out.isReady) {
                    if (!content.hasRemaining()) {
                        response.status = 200
                        async.complete()
                        return
                    }
                    out.write(content.get().toInt())
                }
            }

            override fun onError(t: Throwable) {
                servletContext.log("Async Error", t)
                async.complete()
            }
        })
    }

    companion object {
        private const val HEAVY_RESOURCE = "This is some heavy resource that will be served in an async way"
    }
}