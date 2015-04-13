package com.namazustudios.promotion.rest.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.promotion.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.promotion.guice.ConfigurationModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Created by patricktwohig on 3/19/15.
 */
@WebListener
public class GuiceMain extends GuiceServletContextListener {



    private Injector injector;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        final ServletContext context = servletContextEvent.getServletContext();
        context.setAttribute(GuiceResourceConfig.INJECOR_ATTRIBUTE_NAME, injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        final ServletContext context = servletContextEvent.getServletContext();
        context.removeAttribute(GuiceResourceConfig.INJECOR_ATTRIBUTE_NAME);
        injector = null;
    }

    @Override
    protected Injector getInjector() {
        return injector = Guice.createInjector(
                new JerseyModule(),
                new ServicesModule(),
                new MongoDaoModule(),
                new ConfigurationModule()
        );
    }

}
