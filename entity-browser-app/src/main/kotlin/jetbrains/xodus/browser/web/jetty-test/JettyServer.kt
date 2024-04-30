package jetbrains.xodus.browser.web.`jetty-test`

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.util.thread.QueuedThreadPool


internal class JettyServer {
    private var server: Server? = null

    @Throws(Exception::class)
    fun start() {
        val maxThreads = 100
        val minThreads = 10
        val idleTimeout = 120

        val threadPool = QueuedThreadPool(maxThreads, minThreads, idleTimeout)

        server = Server(threadPool)
        val connector = ServerConnector(server)
        connector.port = 8090
        server!!.connectors = arrayOf<Connector>(connector)

        val servletHandler = ServletHandler()
        server!!.handler = servletHandler

        servletHandler.addServletWithMapping(BlockingServlet::class.java, "/status")
        servletHandler.addServletWithMapping(AsyncServlet::class.java, "/heavy/async")

        server!!.start()
    }

    @Throws(Exception::class)
    fun stop() {
        server!!.stop()
    }
}