package com.lehvolk.xodus.web

import mu.KLogging
import java.io.IOException
import java.io.InputStreamReader


fun main(args: Array<String>) {
    val port = Integer.getInteger("server.port", 18080)
    Application.start()

    HttpServer.setup(port)

    OS.launchBrowser(port)
}

private object OS : KLogging() {

    fun launchBrowser(port: Int) {
        val url = "http://$hostName:$port"
        logger.info { "try to open browser for '$url'" }
        try {
            val osName = "os.name".system()
            if (osName.startsWith("Mac OS")) {
                logger.info("mac os detected");
                val fileMgr = Class.forName("com.apple.eio.FileManager")
                val openURL = fileMgr.getDeclaredMethod("openURL", String::class.java)
                openURL.invoke(null, url)
            } else if (osName.startsWith("Windows")) {
                logger.info("windows detected");
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url)
            } else {
                // Unix or Linux
                logger.info("linux detected");
                val browsers = arrayOf("google-chrome", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape")
                val selectedBrowser: String? = browsers.firstOrNull { Runtime.getRuntime().exec(arrayOf("which", it)).waitFor() == 0 }
                if (selectedBrowser == null) {
                    throw Exception("Couldn't find web browser")
                } else {
                    logger.info("open url using browser {}", selectedBrowser);
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
}
