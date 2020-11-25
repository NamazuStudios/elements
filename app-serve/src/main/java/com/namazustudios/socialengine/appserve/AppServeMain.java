package com.namazustudios.socialengine.appserve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.appserve.guice.*;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTDaoModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.dao.rt.guice.RTGitApplicationModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQAsyncConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ZContextModule;
import com.namazustudios.socialengine.service.guice.AppleIapReceiptInvokerModule;
import com.namazustudios.socialengine.service.guice.GameOnInvokerModule;
import com.namazustudios.socialengine.service.guice.JacksonHttpClientModule;
import com.namazustudios.socialengine.service.guice.OctetStreamJsonMessageBodyReader;
import com.namazustudios.socialengine.util.AppleDateFormat;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import javax.inject.Inject;
import java.text.DateFormat;
import java.util.List;
import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.google.inject.Guice.createInjector;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;
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

    private static final Server createServer(final String[] args) {


        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Supplier<List<FacebookPermission>> facebookPermissionListSupplier;
        facebookPermissionListSupplier =  new FacebookBuiltinPermissionsSupplier();

        return createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new MongoCoreModule(),
            new ServerModule(),
            new AppServeFilterModule(),
            new AppServeSecurityModule(),
            new AppServeServicesModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new ZContextModule(),
            new GameOnInvokerModule(),
            new RTFilesystemGitLoaderModule(),
            new RTDaoModule(),
            new RTGitApplicationModule(),
            new AppServeRedissonServicesmodule(),
            new JeroMQRemoteInvokerModule(),
            new JeroMQInstanceConnectionServiceModule(),
            new JeroMQAsyncConnectionServiceModule(),
            new SimpleRemoteInvokerRegistryModule(),
            new FSTPayloadReaderWriterModule(),
            new InstanceDiscoveryServiceModule(defaultConfigurationSupplier),
            new FacebookBuiltinPermissionsModule(facebookPermissionListSupplier),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(PersistenceStrategy.class).toInstance(getNullPersistence());
                }
            },
            new AppleIapReceiptInvokerModule(),
            new JacksonHttpClientModule()
                .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
                .withDefaultObjectMapperProvider(() -> {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return objectMapper;
                }).withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    final DateFormat dateFormat = new AppleDateFormat();
                    objectMapper.setDateFormat(dateFormat);
                    objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return objectMapper;
                })
        ).getInstance(Server.class);

    }

}
