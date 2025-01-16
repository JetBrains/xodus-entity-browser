package jetbrains.xodus.browser.web

import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.xodus.browser.web.EnvironmentFactory.persistentEntityStore
import java.nio.file.Files
import kotlin.io.path.absolutePathString

fun main() {
    Home.setup()
    val appPort = Integer.getInteger("server.port", 18080)
    val appHost = System.getProperty("server.host", "localhost")
    val context = System.getProperty("server.context", "/")

    val dbSummary = DBSummary(location = Files.createTempDirectory("some path to app").absolutePathString())
    val store = persistentEntityStore(dbSummary)

    val server = embeddedServer(Jetty, port = appPort, host = appHost) {
        val webApplication = object : EmbeddableWebApplication(lookup = { listOf(store) }) {

            override fun PersistentEntityStore.isForcedlyReadonly() = true

        }
        HttpServer(webApplication, context).setup(this)
    }
    server.start(false)

    Browser.launch("http://$appHost:$appPort$context")
}
