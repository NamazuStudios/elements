package com.namazustudios.socialengine.server.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Created by patricktwohig on 5/11/17.
 */
public class UiGuiceServletContextListener extends GuiceServletContextListener {

    private Injector injector;

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
        servletContext.setAttribute(UiGuiceResourceConfig.INJECOR_ATTRIBUTE_NAME, injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        servletContext.removeAttribute(UiGuiceResourceConfig.INJECOR_ATTRIBUTE_NAME);
        servletContext = null;
        injector = null;
    }

    @Override
    protected Injector getInjector() {
        return injector =  Guice.createInjector(
            new UIServicesModule(),
            new UiGuiceServletModule(),
            new UiGuiceConfigurationModule(servletContext::getClassLoader)
        );
    }

}
