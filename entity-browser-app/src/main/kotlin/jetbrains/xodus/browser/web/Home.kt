package jetbrains.xodus.browser.web

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Home {

    val appHome by lazy {
        val userHome = System.getProperty("user.home")
        val appHome = File(userHome, ".xodus-entity-browser")
        if (appHome.exists()) {
            appHome.mkdirs()
        }
        appHome
    }

    val dbHome by lazy {
        File(appHome, "db")
    }

    val logsHome by lazy {
        File(appHome, "logs")
    }

    fun setup() {
        logsHome.takeIf { !it.exists() }?.mkdirs()
        dbHome.takeIf { !it.exists() }?.mkdirs()
        setupLogging()
    }

    private fun setupLogging() {
        val date = SimpleDateFormat("yyyy-MM-dd_hh_mm_ss").format(Date())
        System.setProperty("org.slf4j.simpleLogger.logFile", File(logsHome, "browser-$date.logs").absolutePath)
//        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
    }
}