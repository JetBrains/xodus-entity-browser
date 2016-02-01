package com.lehvolk.xodus.web


import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class AppServletContextListener : ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent?) {
        PersistentStoreService.construct()
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        PersistentStoreService.destroy()
    }
}
