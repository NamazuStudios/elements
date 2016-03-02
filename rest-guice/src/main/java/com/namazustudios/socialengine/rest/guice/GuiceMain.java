package com.namazustudios.socialengine.rest.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.apache.bval.guice.ValidationModule;

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

        final String apiRoot = "/api";

        return injector = Guice.createInjector(
                new ConfigurationModule(),
                new JerseyModule(apiRoot) {
                    @Override
                    protected void configureResoures() {
                        enableAllResources();
                    }
                },
                new ShortLinkFilterModule(apiRoot),
                new ServicesModule(),
                new MongoDaoModule(),
                new MongoSearchModule(),
                new ValidationModule()
        );

    }

}
