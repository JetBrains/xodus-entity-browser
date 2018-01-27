package com.lehvolk.xodus.web

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import javax.servlet.ServletConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
private const val mapping = "/api/*"

object ApplicationConfig : ResourceConfig(
        JacksonConfigurator::class.java,
        ValidationErrorMapper::class.java,
        EntityExceptionMapper::class.java,
        ServerExceptionMapper::class.java) {

    init {
        packages("com.lehvolk.xodus.web")
    }
}

@WebServlet(urlPatterns = [mapping], loadOnStartup = 1)
class EntityBrowserServlet : ServletContainer(ApplicationConfig) {

    override fun init(config: ServletConfig?) {
        super.init(config)
        Application.start()
        Thread(Runnable {
            launchBrowser()
        }).start()
    }


    override fun destroy() {
        super.destroy()
        Application.stop()
    }
}

@WebServlet(urlPatterns = ["/databases/*"], loadOnStartup = 1)
class IndexHtml : HttpServlet() {

    private val indexHtml by lazy {
        val inputStream = servletConfig.servletContext.getResourceAsStream("/index.html")
        inputStream.reader().readText()
    }

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        resp!!.writer.write(indexHtml)
    }

}

private fun launchBrowser() {
    val port = Integer.getInteger("server.port", 18080)
    val url = "http://$hostName:$port"
    log.info("try to open browser for '{}'", url);
    try {
        val osName = "os.name".system()
        if (osName.startsWith("Mac OS")) {
            log.info("mac os detected");
            val fileMgr = Class.forName("com.apple.eio.FileManager")
            val openURL = fileMgr.getDeclaredMethod("openURL", String::class.java)
            openURL.invoke(null, url)
        } else if (osName.startsWith("Windows")) {
            log.info("windows detected");
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url)
        } else {
            // Unix or Linux
            log.info("linux detected");
            val browsers = arrayOf("google-chrome", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape")
            val selectedBrowser: String? = browsers.firstOrNull { Runtime.getRuntime().exec(arrayOf("which", it)).waitFor() == 0 }
            if (selectedBrowser == null) {
                throw Exception("Couldn't find web browser")
            } else {
                log.info("open url using browser {}", selectedBrowser);
                Runtime.getRuntime().exec(arrayOf(selectedBrowser, url))
            }
        }
    } catch (e: Exception) {
        println("Unable to open browser: " + e.message)
    }
}

private val hostName: String
    get() {
        try {
            return InputStreamReader(Runtime.getRuntime().exec("hostname").inputStream).readLines().first()
        } catch (ignored: IOException) {
        }
        return "localhost"
    }