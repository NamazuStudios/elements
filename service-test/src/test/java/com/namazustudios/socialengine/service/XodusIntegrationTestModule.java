package com.namazustudios.socialengine.service;

import com.google.inject.Module;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.rt.guice.ResourceScope;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.service.guice.AppleIapReceiptInvokerModule;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.ArrayList;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.service.RedissonClientProvider.REDIS_URL;
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
