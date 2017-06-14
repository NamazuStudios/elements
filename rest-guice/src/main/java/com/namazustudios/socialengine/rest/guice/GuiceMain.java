package com.namazustudios.socialengine.rest.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.apache.bval.guice.ValidationModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.util.Properties;

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
        servletContext.setAttribute(GuiceResourceConfig.INJECOR_ATTRIBUTE_NAME, injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        servletContext.removeAttribute(GuiceResourceConfig.INJECOR_ATTRIBUTE_NAME);
        servletContext = null;
        injector = null;
    }

    @Override
    protected Injector getInjector() {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier(servletContext.getClassLoader());

        final Properties properties = defaultConfigurationSupplier.get();
        final String apiRoot = properties.getProperty(Constants.API_PREFIX);

        return injector = Guice.createInjector(
                new ConfigurationModule(defaultConfigurationSupplier),
                new JerseyModule(apiRoot) {
                    @Override
                    protected void configureResoures() {
                        enableAllResources();
                    }
                },
                new ServicesModule(),
                new MongoDaoModule(),
                new MongoSearchModule(),
                new ValidationModule()
        );

    }

}
