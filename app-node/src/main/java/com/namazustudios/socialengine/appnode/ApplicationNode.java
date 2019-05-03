package com.namazustudios.socialengine.appnode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appnode.guice.*;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.MultiNodeContainer;
import com.namazustudios.socialengine.service.firebase.guice.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.service.notification.guice.GuiceStandardNotificationFactoryModule;
import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.interrupted;

public class ApplicationNode {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationNode.class);

    private final Injector injector;

    public ApplicationNode(DefaultConfigurationSupplier defaultConfigurationSupplier) {
        this.injector = Guice.createInjector(
                new ConfigurationModule(defaultConfigurationSupplier),
                new MongoCoreModule(),
                new MongoDaoModule(),
                new ValidationModule(),
                new MongoSearchModule(),
                new RTFilesystemGitLoaderModule(),
                new MultiNodeContainerModule(),
                new FirebaseAppFactoryModule(),
                new GuiceStandardNotificationFactoryModule(),
                new JaxRSClientModule(),
                new VersionModule(),
                new ServicesModule()
        );
    }

    /**
     * Starts the ApplicationNode. Note: this is a thread-blocking method.
     */
    public void start() {
        final Object lock = new Object();

        try (final MultiNodeContainer container = injector.getInstance(MultiNodeContainer.class)) {

            logger.info("Starting container.");

            container.start();
            logger.info("Container started.");

            synchronized (lock) {
                while (!interrupted()) {
                    lock.wait();
                }
            }

        } catch (InterruptedException ex) {
            logger.info("Interrupted.  Shutting down.");
        }

        logger.info("Container shut down.  Exiting process.");
    }
}
