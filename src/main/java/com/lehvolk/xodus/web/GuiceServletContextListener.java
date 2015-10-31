package com.lehvolk.xodus.web;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.lehvolk.xodus.repo.ConfigurationService;
import com.lehvolk.xodus.repo.PresentationService;
import com.lehvolk.xodus.repo.RepoService;
import com.squarespace.jersey2.guice.JerseyGuiceServletContextListener;
import ru.vyarus.guice.ext.core.method.AnnotatedMethodTypeListener;
import ru.vyarus.guice.ext.managed.PostConstructAnnotationProcessor;
import ru.vyarus.guice.ext.managed.PreDestroyAnnotationProcessor;
import ru.vyarus.guice.ext.managed.destroyable.DestroyableManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.List;

import static java.util.Collections.singletonList;

public class GuiceServletContextListener extends JerseyGuiceServletContextListener {

    @Override
    protected List<? extends Module> modules() {
		final DestroyableManager manager = new DestroyableManager();
        return singletonList(new ServletModule() {

            @Override
            protected void configureServlets() {
                bind(RepoService.class).in(Singleton.class);
				bind(PresentationService.class).in(Singleton.class);
				bind(ConfigurationService.class).in(Singleton.class);;
				bindListener(Matchers.any(), new AnnotatedMethodTypeListener<>(
						PostConstruct.class, new PostConstructAnnotationProcessor()));
				bindListener(Matchers.any(), new AnnotatedMethodTypeListener<>(
						PreDestroy.class, new PreDestroyAnnotationProcessor(manager)));
				bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
				bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
            }
        });
    }

}
