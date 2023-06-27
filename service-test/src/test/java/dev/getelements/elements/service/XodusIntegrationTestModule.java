package dev.getelements.elements.service;

import com.google.inject.Module;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.guice.ResourceScope;
import dev.getelements.elements.rt.lua.guice.LuaModule;
import dev.getelements.elements.rt.remote.guice.ClusterContextFactoryModule;
import dev.getelements.elements.service.guice.AppleIapReceiptInvokerModule;
import dev.getelements.elements.service.guice.RedissonServicesModule;
import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.ArrayList;

import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static dev.getelements.elements.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT_PATH;
import static dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT_PATH;
import static dev.getelements.elements.service.RedissonClientProvider.REDIS_URL;
import static java.lang.String.format;

public class XodusIntegrationTestModule extends AbstractIntegrationTestModule {

    @Override
    public EmbeddedTestService embeddedTestService(final int mongoPort, final int redisPort, final int nodePort) {

        final var supplier = new DefaultConfigurationSupplier();
        final var properties = supplier.getDefaultProperties();

        properties.put(REDIS_URL, format("redis://127.0.0.1:%d", redisPort));
        properties.put(MONGO_CLIENT_URI, format("mongodb://127.0.0.1:%d", mongoPort));

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
                    modules.add(new FirebaseAppFactoryModule());
                    modules.add(new MongoSearchModule());
//                    modules.add(new AppNodeServicesModule());
//                    modules.add(new AppNodeSecurityModule());
                    modules.add(new ClasspathAssetLoaderModule().withDefaultPackageRoot());
                    modules.add(new ConfigurationModule(() -> properties));
                    modules.add(new AppleIapReceiptInvokerModule());
                    modules.add(new ClusterContextFactoryModule());
                    modules.add(new RedissonServicesModule(ResourceScope.getInstance()));
                    modules.add(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
                    return modules;
                })
                .withXodusWorker()
                .withWorkerBindAddress(format("tcp://127.0.0.1:%d", nodePort))
                .withDefaultHttpClient();
    }

}
