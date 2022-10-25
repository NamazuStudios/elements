package com.namazustudios.socialengine.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.namazustudios.socialengine.appnode.guice.AppNodeSecurityModule;
import com.namazustudios.socialengine.appnode.guice.AppNodeServicesModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rest.guice.EmbeddedRestAPIModule;
import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.rt.guice.ResourceScope;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.service.guice.AppleIapReceiptInvokerModule;
import com.namazustudios.socialengine.service.guice.GameOnInvokerModule;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import redis.embedded.RedisServer;
import ru.vyarus.guice.validator.ValidationModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.HTTP_PORT;
import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static com.namazustudios.socialengine.rest.ClientContext.CONTEXT_APPLICATION;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.service.RedissonClientProvider.REDIS_URL;
import static de.flapdoodle.embed.mongo.MongodStarter.getDefaultInstance;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

public class XodusEmbeddedRestApiIntegrationTestModule extends AbstractModule {

    private static final int TEST_MONGO_PORT = 45000;

    private static final String TEST_MONGO_BIND_IP = "127.0.0.1";

    private static final int TEST_REDIS_PORT = 45001;

    private static final String TEST_REDIS_BIND_IP = "127.0.0.1";

    private static final int TEST_APP_NODE_PORT = 45002;

    private static final String TEST_APP_NODE_BIND_IP = "127.0.0.1";

    @Override
    protected void configure() {

        final var configurationSupplier = new DefaultConfigurationSupplier() {
            @Override
            public Properties get() {
                final var properties = super.get();
                properties.put(HTTP_PORT, "8081");
                properties.put(TEST_API_ROOT, "http://localhost:8081/api/rest");
                properties.put(REDIS_URL, format("redis://%s:%d", TEST_REDIS_BIND_IP, TEST_REDIS_PORT));
                properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_MONGO_BIND_IP, TEST_MONGO_PORT));
                properties.put(STATIC_HOST_INFO, format("tcp://%s:%d", TEST_APP_NODE_BIND_IP, TEST_APP_NODE_PORT));
                return properties;
            }
        };

        try {
            final var executable = mongodExecutable();
            getRuntime().addShutdownHook(new Thread(executable::stop));
            bind(MongodExecutable.class).toInstance(executable);
            bind(MongodProcess.class).toInstance(executable.start());
        } catch (Exception e) {
            addError(e);
            return;
        }

        try {
            final var redisServer = new RedisServer(TEST_REDIS_PORT);
            getRuntime().addShutdownHook(new Thread(redisServer::stop));
            redisServer.start();
            bind(RedisServer.class).toInstance(redisServer);
        } catch (Exception e) {
            addError(e);
            return;
        }

        try {
            final var embeddedTestService = embeddedTestService();
            getRuntime().addShutdownHook(new Thread(embeddedTestService::close));
            embeddedTestService.start();
            bind(EmbeddedTestService.class).toInstance(embeddedTestService);
        } catch (Exception ex) {
            addError(ex);
            return;
        }

        final var restApiMainProvider = getProvider(RestAPIMain.class);
        final var applicationDaoProvider = getProvider(ApplicationDao.class);
        final var embeddedTestServiceProvider = getProvider(EmbeddedTestService.class);

        bind(Application.class).annotatedWith(named(CONTEXT_APPLICATION)).toProvider(() -> {

            final var application = new Application();
            application.setName("CXTTAPP");
            application.setDescription("Context Test Application");

            final var created = applicationDaoProvider.get().createOrUpdateInactiveApplication(application);
            final var worker = embeddedTestServiceProvider.get().getWorker().getWorker();

            try (var mutation = worker.beginMutation()) {
                mutation.addNode(created.getId());
                mutation.commit();
            }

            restApiMainProvider.get().getInstance().refreshConnections();
            return created;

        }).in(SINGLETON);

        install(new RestAPITestServerModule());
        install(new EmbeddedRestAPIModule(configurationSupplier));

        bind(RestAPIMain.class).asEagerSingleton();
        bind(EmbeddedRestApi.class).asEagerSingleton();

    }

    public MongodExecutable mongodExecutable() throws IOException {

        final var config = MongodConfig.builder()
                .version(Version.V3_4_5)
                .net(new Net(TEST_MONGO_BIND_IP, TEST_MONGO_PORT, localhostIsIPv6()))
            .build();

        final var starter = getDefaultInstance();
        return starter.prepare(config);

    }

    public EmbeddedTestService embeddedTestService() {

        final var supplier = new DefaultConfigurationSupplier();
        final var properties = supplier.getDefaultProperties();

        properties.put(REDIS_URL, format("redis://%s:%d", TEST_REDIS_BIND_IP, TEST_REDIS_PORT));
        properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_MONGO_BIND_IP, TEST_MONGO_PORT));

        properties.remove(RESOURCE_ENVIRONMENT_PATH);
        properties.remove(SCHEDULER_ENVIRONMENT_PATH);

        final var facebookPermissionSupplier = new FacebookBuiltinPermissionsSupplier();

        return new JeroMQEmbeddedTestService()
            .withNodeModuleFactory(nodeId -> {
                final var modules = new ArrayList<Module>();
                modules.add(new LuaModule());
                modules.add(new ValidationModule());
                modules.add(new MongoCoreModule());
                modules.add(new MongoDaoModule());
                modules.add(new MongoSearchModule());
                modules.add(new AppNodeServicesModule());
                modules.add(new FirebaseAppFactoryModule());
                modules.add(new ClasspathAssetLoaderModule().withDefaultPackageRoot());
                modules.add(new AppNodeSecurityModule());
                modules.add(new ConfigurationModule(() -> properties));
                modules.add(new AppleIapReceiptInvokerModule());
                modules.add(new GameOnInvokerModule());
                modules.add(new ClusterContextFactoryModule());
                modules.add(new RedissonServicesModule(ResourceScope.getInstance()));
                modules.add(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
                return modules;
            })
            .withXodusWorker()
            .withWorkerBindAddress(format("tcp://%s:%d", TEST_APP_NODE_BIND_IP, TEST_APP_NODE_PORT))
            .withDefaultHttpClient();
    }

}
