package com.namazustudios.socialengine.rest.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Created by patricktwohig on 3/19/15.
 */
@WebListener
public class GuiceMain extends GuiceServletContextListener {

    private Injector injector;

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
        servletContext.setAttribute(GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);

        final ConnectionMultiplexer connectionMultiplexer = injector.getInstance(ConnectionMultiplexer.class);
        connectionMultiplexer.start();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        super.contextDestroyed(servletContextEvent);

        final ConnectionMultiplexer connectionMultiplexer = injector.getInstance(ConnectionMultiplexer.class);
        connectionMultiplexer.stop();

        servletContext.removeAttribute(GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME);
        servletContext = null;
        injector = null;

    }

    @Override
    protected Injector getInjector() {
        final RestAPIModule restAPIModule = new RestAPIModule(servletContext.getClassLoader());
        return injector = Guice.createInjector(restAPIModule);
    }

}
