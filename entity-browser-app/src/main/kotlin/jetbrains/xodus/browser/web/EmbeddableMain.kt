package jetbrains.xodus.browser.web

import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.xodus.browser.web.db.EnvironmentFactory
import jetbrains.xodus.browser.web.db.EnvironmentParameters
import java.nio.file.Files
import kotlin.io.path.absolutePathString

fun main() {
    Home.setup()
    val appPort = Integer.getInteger("server.port", 18080)
    val appHost = System.getProperty("server.host", "localhost")
    val context = System.getProperty("server.context", "/")

    val params = EnvironmentParameters(location = Files.createTempDirectory("some path to app").absolutePathString())
    val environment = EnvironmentFactory.createEnvironment(params)

    val server = embeddedServer(Jetty, port = appPort, host = appHost) {
        val webApplication = object : EmbeddableWebApplication(lookup = { listOf(environment) }) {

            override fun PersistentEntityStore.isForcedlyReadonly() = true

        }
        HttpServer(webApplication, context).setup(this)
    }
    server.start(false)

    Browser.launch("http://$appHost:$appPort$context")
}
