package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.MongoTestInstanceModule;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.rt.fst.FSTPayloadReaderWriterModule;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.remote.guice.ClusterContextFactoryModule;
import dev.getelements.elements.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import dev.getelements.elements.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import dev.getelements.elements.rt.remote.jeromq.guice.*;
import dev.getelements.elements.service.guice.*;
import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.morphia.Datastore;
import redis.embedded.RedisServer;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.concurrent.atomic.AtomicInteger;

import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static dev.getelements.elements.model.blockchain.BlockchainNetwork.FLOW;
import static dev.getelements.elements.model.blockchain.BlockchainNetwork.FLOW_TEST;
import static dev.getelements.elements.rt.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static dev.getelements.elements.service.RedissonClientProvider.REDIS_URL;
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
            properties.put(FLOW.urlsName(), "grpc://localhost:3569");
            properties.put(FLOW_TEST.urlsName(), "grpc://localhost:3569");
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

