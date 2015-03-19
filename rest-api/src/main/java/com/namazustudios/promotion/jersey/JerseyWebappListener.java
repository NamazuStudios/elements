package com.namazustudios.promotion.jersey;

import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Set;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class JerseyWebappListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();




    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
