package com.namazustudios.socialengine.appnode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appnode.guice.*;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.guice.SimpleExecutorsModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.guice.PersistentInstanceIdModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.*;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import com.namazustudios.socialengine.service.guice.GuiceStandardNotificationFactoryModule;
import com.namazustudios.socialengine.service.guice.JacksonHttpClientModule;
import com.namazustudios.socialengine.service.guice.OctetStreamJsonMessageBodyReader;
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
import static java.lang.Thread.interrupted;

public class ApplicationNode {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNode.class);

    private final Injector injector;

    public ApplicationNode(final DefaultConfigurationSupplier defaultConfigurationSupplier) {
        this.injector = Guice.createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new InstanceDiscoveryServiceModule(defaultConfigurationSupplier),
            new FSTPayloadReaderWriterModule(),
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
            new RTFilesystemGitLoaderModule(),
            new WorkerInstanceModule(),
            new FirebaseAppFactoryModule(),
            new GuiceStandardNotificationFactoryModule(),
            new VersionModule(),
            new ServicesModule(),
            new SimpleExecutorsModule().withDefaultSchedulerThreads(),
            new UnixFSTransactionalPersistenceContextModule().withChecksumAlgorithm(ADLER_32),
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
