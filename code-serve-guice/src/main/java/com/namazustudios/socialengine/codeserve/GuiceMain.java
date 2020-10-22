package com.namazustudios.socialengine.codeserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static com.google.inject.Guice.createInjector;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class GuiceMain extends GuiceServletContextListener {

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        servletContext = null;
    }

    @Override
    protected Injector getInjector() {
        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier(servletContext.getClassLoader());
        return createInjector(new CodeServeModule((defaultConfigurationSupplier)));
    }

}
