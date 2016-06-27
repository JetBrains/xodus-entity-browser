package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.resources.DB_COOKIE
import com.lehvolk.xodus.web.resources.switchTo
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
private const val mapping = "/api/*"

object ApplicationConfig : ResourceConfig(
        JacksonConfigurator::class.java,
        ValidationErrorMapper::class.java,
        EntityExceptionMapper::class.java,
        CommonExceptionMapper::class.java) {

    init {
        packages("com.lehvolk.xodus.web")
    }
}

@WebServlet(urlPatterns = arrayOf(mapping), loadOnStartup = 1)
class EntityBrowserServlet : ServletContainer(ApplicationConfig) {

    override fun init(config: ServletConfig?) {
        super.init(config)
        InjectionContexts.start()
        Thread(Runnable {
            launchBrowser()
        }).start()
    }


    override fun destroy() {
        super.destroy()
        InjectionContexts.destroy()
    }
}

@WebFilter(urlPatterns = arrayOf(mapping))
class EntityBrowserFilter : Filter {

    override fun destroy() {
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        var uuid = (request as HttpServletRequest).cookies.find { it.name == DB_COOKIE }?.value
        if (uuid == null) {
            if (Databases.firstOpened() != null) {
                uuid = Databases.firstOpened()!!.uuid
                Databases.current(uuid)
            }
            if (uuid != null) {
                (response as HttpServletResponse).switchTo(uuid)
            }
        } else {
            Databases.current(uuid)
        }
        chain.doFilter(request, response)
    }

    override fun init(filterConfig: FilterConfig) {
    }

}


private fun launchBrowser() {
    val port = Integer.getInteger("server.port", 8080)
    val url = "http://${hostName()}:$port"
    log.info("try to open browser for '{}'", url);
    try {
        val osName = System.getProperty("os.name")
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
            var selectedBrowser: String? = null
            for (browser in browsers) {
                if (Runtime.getRuntime().exec(arrayOf("which", browser)).waitFor() == 0) {
                    selectedBrowser = browser
                    break
                }
            }
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

private fun hostName(): String {
    try {
        return InputStreamReader(Runtime.getRuntime().exec("hostname").inputStream).readLines().first()
    } catch (ignored: IOException) {
    }
    return "localhost"
}