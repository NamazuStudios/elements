package com.namazustudios.socialengine.appnode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appnode.guice.AppNodeSecurityModule;
import com.namazustudios.socialengine.appnode.guice.AppNodeServicesModule;
import com.namazustudios.socialengine.appnode.guice.MasterNodeModule;
import com.namazustudios.socialengine.appnode.guice.WorkerInstanceModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.git.FilesystemGitLoaderModule;
import com.namazustudios.socialengine.rt.guice.ResourceScope;
import com.namazustudios.socialengine.rt.guice.SimpleExecutorsModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Worker;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.guice.PersistentInstanceIdModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.*;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.service.guice.*;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.util.AppleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import java.text.DateFormat;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSChecksumAlgorithm.ADLER_32;

public class ApplicationNode {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNode.class);

    private Worker worker;

    private Instance instance;

    private final Injector injector;

    private final Object lock = new Object();

    public ApplicationNode(final DefaultConfigurationSupplier defaultConfigurationSupplier) {

        final var facebookBuiltinPermissionsSupplier = new FacebookBuiltinPermissionsSupplier();

        injector = Guice.createInjector(
            new ClusterContextFactoryModule(),
            new ConfigurationModule(defaultConfigurationSupplier),
            new InstanceDiscoveryServiceModule(defaultConfigurationSupplier),
            new FSTPayloadReaderWriterModule(),
            new AppNodeSecurityModule(),
            new FacebookBuiltinPermissionsModule(facebookBuiltinPermissionsSupplier),
            new PersistentInstanceIdModule(),
            new ZContextModule(),
            new MasterNodeModule(),
            new SimpleTransactionalResourceServicePersistenceModule(),
            new JeroMQRemoteInvokerModule(),
            new JeroMQAsyncConnectionServiceModule(),
            new JeroMQInstanceConnectionServiceModule(),
            new JeroMQControlClientModule(),
            new MongoCoreModule(),
            new SimpleRemoteInvokerRegistryModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new FilesystemGitLoaderModule(),
            new WorkerInstanceModule(),
            new FirebaseAppFactoryModule(),
            new GuiceStandardNotificationFactoryModule(),
            new SimpleExecutorsModule().withDefaultSchedulerThreads(),
            new UnixFSTransactionalPersistenceContextModule().withChecksumAlgorithm(ADLER_32),
            new XodusEnvironmentModule().withSchedulerEnvironment(),
            new AppNodeServicesModule(),
            new RedissonServicesModule(ResourceScope.getInstance()),
            new AppleIapReceiptInvokerModule(),
            new GameOnInvokerModule(),
            new JacksonHttpClientModule()
                .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
                .withDefaultObjectMapperProvider(() -> {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return objectMapper;
                })
                .withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    final DateFormat dateFormat = new AppleDateFormat();
                    objectMapper.setDateFormat(dateFormat);
                    objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return objectMapper;
                })
        );

    }

    /**
     * Gets the {@link Worker} for this {@link ApplicationNode}.
     *
     * @return the {@link Worker}
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Starts the ApplicationNode.
     */
    public void start() {
        synchronized (lock) {

            if (worker != null) throw new IllegalStateException("Already running.");
            lock.notifyAll();

            worker = injector.getInstance(Worker.class);
            instance = injector.getInstance(Instance.class);

            try {

                logger.info("Starting Instance.");
                instance.start();

                logger.info("Instance started.");

            } catch (Exception ex) {
                worker = null;
                instance = null;
                logger.error("Could not start ApplicationNode", ex);
                throw ex;
            }

        }
    }

    /**
     * Stops the ApplicationNode.
     */
    public void stop() {
        synchronized (lock) {

            if (worker == null) throw new IllegalStateException("Not currently running.");
            lock.notifyAll();

            try {
                logger.info("Starting Instance.");
                instance.close();
                logger.info("Instance started.");
            } catch (Exception ex) {
                logger.error("Could not start ApplicationNode", ex);
                throw ex;
            } finally {
                worker = null;
                instance = null;
            }

        }
    }

    /**
     * Waits for the ApplicationNode to shut down.
     */
    public void waitForShutdown() throws InterruptedException {
        synchronized (lock) {
            while (worker != null) {
                lock.wait();
            }
        }
    }

}
