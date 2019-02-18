package jetbrains.xodus.browser.web

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import jetbrains.xodus.browser.web.db.PersistentDatabaseService
import mu.KLogging

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
    OS.launchBrowser(appHost, appPort, context)
}

internal object OS : KLogging() {

    fun launchBrowser(host: String, port: Int, context: String) {
        val url = "http://$host:$port$context"
        logger.info { "try to open browser for '$url'" }
        try {
            val osName = "os.name".system()
            if (osName.startsWith("Mac OS")) {
                logger.info("mac os detected")
                val fileMgr = Class.forName("com.apple.eio.FileManager")
                val openURL = fileMgr.getDeclaredMethod("openURL", String::class.java)
                openURL.invoke(null, url)
            } else if (osName.startsWith("Windows")) {
                logger.info("windows detected")
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler $url")
            } else {
                // Unix or Linux
                logger.info("linux detected")
                val browsers = arrayOf("google-chrome", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape")
                val selectedBrowser: String? = browsers.firstOrNull { Runtime.getRuntime().exec(arrayOf("which", it)).waitFor() == 0 }
                if (selectedBrowser == null) {
                    throw Exception("Couldn't find web browser")
                } else {
                    logger.info { "open url using browser $selectedBrowser" }
                    Runtime.getRuntime().exec(arrayOf(selectedBrowser, url))
                }
            }
        } catch (e: Exception) {
            println("Unable to open browser: " + e.message)
        }
    }

}
