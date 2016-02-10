package com.lehvolk.xodus.web

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import javax.servlet.ServletConfig
import javax.ws.rs.core.Application

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