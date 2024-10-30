package jetbrains.xodus.browser.web

import mu.KLogging
import java.awt.Desktop
import java.net.URI

internal object Browser : KLogging() {

    fun launch(url: String) {
        try {
            val osName = System.getProperty("os.name")
            if (osName.startsWith("Mac OS")) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI.create(url))
                } else {
                    Runtime.getRuntime().exec("open $url")
                }
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler $url")
            } else {
                // Unix or Linux
                var selectedBrowser: String? = null
                for (browser in arrayOf("google-chrome", "firefox", "opera", "mozilla")) {
                    if (Runtime.getRuntime().exec(arrayOf("which", browser)).waitFor() == 0) {
                        selectedBrowser = browser
                        break
                    }
                }
                if (selectedBrowser == null) {
                    throw Exception("Couldn't find web browser")
                } else {
                    Runtime.getRuntime().exec(arrayOf(selectedBrowser, url))
                }
            }
        } catch (e: Exception) {
            logger.error("Unable to open browser: ", e)
        }
    }
}