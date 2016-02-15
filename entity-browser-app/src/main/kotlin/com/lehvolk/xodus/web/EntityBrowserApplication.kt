package com.lehvolk.xodus.web

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import javax.servlet.ServletConfig
import javax.ws.rs.core.Application

private val log = LoggerFactory.getLogger(EntityBrowserApplication::class.java)

class JerseyEntityBrowserApplication : ResourceConfig() {

    init {
        packages("com.lehvolk.xodus.web")
    }
}

class EntityBrowserApplication : Application() {

    override fun getClasses(): Set<Class<*>>? {
        return setOf(
                JacksonConfigurator::class.java,
                ValidationErrorMapper::class.java,
                EntityExceptionMapper::class.java,
                CommonExceptionMapper::class.java
        )
    }

}

class EntityBrowserServlet : ServletContainer() {

    override fun init(config: ServletConfig?) {
        super.init(config)
        DI.persistenceService.construct()
        Thread(Runnable {
            launchBrowser()
        }).start()
    }

    override fun destroy() {
        super.destroy()
        DI.persistenceService.destroy()
    }
}

object DI {

    val persistenceService by lazy { PersistentStoreService() }
    val presentationService by lazy { PresentationService() }

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