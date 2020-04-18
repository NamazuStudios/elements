package com.namazustudios.socialengine.appserve;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.appserve.guice.*;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import org.apache.bval.guice.ValidationModule;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.google.inject.Guice.createInjector;
import static com.namazustudios.socialengine.rt.PersistenceStrategy.getNullPersistence;

public class AppServeMain implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AppServeMain.class);

    private final Server server;

    public AppServeMain(final String[] args) {
        this(createServer(args));
    }

    @Inject
    public AppServeMain(final Server server) {
        this.server = server;
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        join();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    @Override
    public void run() {
        try {
            start();
            join();
        } catch (Exception ex) {
            logger.error("Encountered error running server.", ex);
        }
    }

    public static void main(final String[] args) throws Exception {
        final AppServeMain main = new AppServeMain(args);
        main.start();
        main.join();
    }

    private static Server createServer(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        return createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new MongoCoreModule(),
            new ServerModule(),
            new AppServeServicesModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new ZContextModule(),
            new JeroMQMultiplexerModule(),
            new RTFilesystemGitLoaderModule(),
            new JaxRSClientModule(),
            new AppServeSecurityModule(),
            new AppServeFilterModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(PersistenceStrategy.class).toInstance(getNullPersistence());
                }
            }
        ).getInstance(Server.class);

    }

}
