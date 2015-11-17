package com.lehvolk.xodus.web;

import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.lehvolk.xodus.web.services.PersistentStoreService;
import com.squarespace.jersey2.guice.JerseyGuiceServletContextListener;
import static java.util.Collections.singletonList;

public class GuiceServletContextListener extends JerseyGuiceServletContextListener {

    @Override
    protected List<? extends Module> modules() {
        return singletonList(new ServletModule() {

            @Override
            protected void configureServlets() {
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
            }
        });
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        getInjector().getInstance(PersistentStoreService.class).construct();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        super.contextDestroyed(sce);
        getInjector().getInstance(PersistentStoreService.class).destroy();
    }
}
