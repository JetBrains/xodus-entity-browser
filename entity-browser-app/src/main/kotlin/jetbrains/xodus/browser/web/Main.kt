package jetbrains.xodus.browser.web

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.plugins.defaultheaders.*
import jetbrains.xodus.browser.web.db.PersistentDatabaseService

fun main() {
    Home.setup()
    val appPort = Integer.getInteger("server.port", 18080)
    val appHost = System.getProperty("server.host", "localhost")
    val context = System.getProperty("server.context", "/")

    val server = embeddedServer(Jetty, port = appPort, host = appHost) {
        val webApplication = PersistentWebApplication(PersistentDatabaseService()).also { it.start() }
        object : HttpServer(webApplication, context) {

            override fun Application.installAdditionalFeatures() {
                install(DefaultHeaders)
            }

        }.setup(this)
    }
    server.start(false)

    Browser.launch("http://$appHost:$appPort$context")
}

