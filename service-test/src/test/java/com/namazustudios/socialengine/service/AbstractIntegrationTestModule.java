package com.namazustudios.socialengine.service;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.MongoTestInstanceModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.jersey.JerseyHttpClientModule;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import com.namazustudios.socialengine.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.*;
import com.namazustudios.socialengine.service.guice.*;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import dev.morphia.Datastore;
import redis.embedded.RedisServer;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.concurrent.atomic.AtomicInteger;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static com.namazustudios.socialengine.service.RedissonClientProvider.REDIS_URL;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

public abstract class AbstractIntegrationTestModule extends AbstractModule {

    private static final AtomicInteger testMongoPort = new AtomicInteger(45100);

    private static final AtomicInteger testRedisPort = new AtomicInteger(45200);

    private static final AtomicInteger testNodePort = new AtomicInteger(45300);

    @Override
    protected void configure() {

        final int nodePort = testNodePort.getAndIncrement();
        final int mongoPort = testMongoPort.getAndIncrement();
        final int redisPort = testRedisPort.getAndIncrement();

        try {
            final var redisServer = new RedisServer(redisPort);
            getRuntime().addShutdownHook(new Thread(redisServer::stop));
            redisServer.start();
            bind(RedisServer.class).toInstance(redisServer);
        } catch (Exception e) {
            addError(e);
            return;
        }

        try {
            final var embeddedTestService = embeddedTestService(mongoPort, redisPort, nodePort);
            getRuntime().addShutdownHook(new Thread(embeddedTestService::close));
            embeddedTestService.start();
            bind(EmbeddedTestService.class).toInstance(embeddedTestService);
        } catch (Exception ex) {
            addError(ex);
            return;
        }

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        install(new ConfigurationModule(() -> {
            final var properties = defaultConfigurationSupplier.get();
            properties.put(REDIS_URL, format("redis://127.0.0.1:%d", redisPort));
            properties.put(MONGO_CLIENT_URI, format("mongodb://127.0.0.1:%d", mongoPort));
            properties.put(STATIC_HOST_INFO, format("tcp://127.0.0.1:%d", nodePort));
            return properties;
        }));

        install(new MongoDaoModule() {
            @Override
            protected void configure() {
                super.configure();
                expose(Datastore.class);
            }
        });

        install(new ZContextModule());
        install(new JeroMQAsyncConnectionServiceModule());
        install(new JeroMQInstanceConnectionServiceModule());
        install(new JeroMQRemoteInvokerModule());
        install(new JeroMQControlClientModule());
        install(new ClusterContextFactoryModule());
        install(new JerseyHttpClientModule());
        install(new MongoTestInstanceModule(mongoPort));
        install(new MongoCoreModule());
        install(new MongoSearchModule());
        install(new ValidationModule());
        install(new AppleIapReceiptInvokerModule());
        install(new IntegrationTestSecurityModule());
        install(new FirebaseAppFactoryModule());
        install(new SimpleRemoteInvokerRegistryModule());
        install(new FSTPayloadReaderWriterModule());
        install(new FacebookBuiltinPermissionsModule(new FacebookBuiltinPermissionsSupplier()));
        install(new GuiceStandardNotificationFactoryModule());
        install(new StaticInstanceDiscoveryServiceModule());

        install(new NotificationServiceModule());
        install(new RedissonServicesModule(TestScope.scope));
        install(new ServicesModule(TestScope.scope, TestScope.AttributesProvider.class));

        bind(InstanceId.class).toInstance(randomInstanceId());

    }

    public abstract EmbeddedTestService embeddedTestService(int mongoPort, int redisPort, int nodePort);

}
