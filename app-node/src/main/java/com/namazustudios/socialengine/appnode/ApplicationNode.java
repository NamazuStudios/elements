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
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.WorkerInstance;
import com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.guice.PersistentInstanceIdModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import com.namazustudios.socialengine.service.firebase.guice.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.service.notification.guice.GuiceStandardNotificationFactoryModule;
import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.interrupted;

public class ApplicationNode {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNode.class);

    private final Injector injector;

    public ApplicationNode(final DefaultConfigurationSupplier defaultConfigurationSupplier) {
        this.injector = Guice.createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new PersistentInstanceIdModule(),
            new ZContextModule(),
            new MasterNodeModule(),
            new JeroMQRemoteInvokerModule().withDefaultExecutorServiceProvider(),
            new JeroMQInstanceConnectionServiceModule(),
            new InstanceDiscoveryServiceModule(defaultConfigurationSupplier),
            new MongoCoreModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new RTFilesystemGitLoaderModule(),
            new WorkerInstanceModule(),
            new FirebaseAppFactoryModule(),
            new GuiceStandardNotificationFactoryModule(),
            new JaxRSClientModule(),
            new VersionModule(),
            new ServicesModule(),
            new FSTPayloadReaderWriterModule()
        );
    }

    /**
     * Starts the ApplicationNode. Note: this is a thread-blocking method.
     */
    public void start() {
        final Object lock = new Object();

        try (final Instance container = injector.getInstance(Instance.class)) {

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
