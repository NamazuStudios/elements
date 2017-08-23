package com.namazustudios.socialengine.codeserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.socialengine.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.dao.rt.guice.RTGitBootstrapModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.apache.bval.guice.ValidationModule;

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

        return createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new RedisModule(),
            new ServicesModule(),
            new MongoCoreModule(),
            new MongoDaoModule(),
            new MongoSearchModule(),
            new ValidationModule(),
            new GitSecurityModule(),
            new GitServletModule(),
            new RTFilesystemGitLoaderModule(),
            new RTGitBootstrapModule(),
            new FileSystemCodeServeModule()
        );

    }

}
