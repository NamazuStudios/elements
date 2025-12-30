package dev.getelements.elements.service;

import com.google.inject.Module;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoGridFSLargeObjectBucketModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.rt.remote.guice.ClusterContextFactoryModule;
import dev.getelements.elements.service.guice.AppleIapReceiptInvokerModule;
import dev.getelements.elements.service.guice.AuthOperationsModule;
import dev.getelements.elements.service.guice.MetaIapReceiptInvokerModule;
import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.ArrayList;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static java.lang.String.format;

public class UnixFSIntegrationTestModule extends AbstractIntegrationTestModule {

    @Override
    public EmbeddedTestService embeddedTestService(final int mongoPort, final int redisPort, final int nodePort) {

        final var supplier = new DefaultConfigurationSupplier();
        final var properties = supplier.getDefaultProperties();

        properties.put(MONGO_CLIENT_URI, format("mongodb://127.0.0.1:%d", mongoPort));

        final var facebookPermissionSupplier = new FacebookBuiltinPermissionsSupplier();

        return new JeroMQEmbeddedTestService()
                .withNodeModuleFactory(nodeId -> {
                    final var modules = new ArrayList<Module>();
                    modules.add(new ValidationModule());
                    modules.add(new MongoCoreModule());
                    modules.add(new MongoGridFSLargeObjectBucketModule());
                    modules.add(new MongoDaoModule());
                    modules.add(new FirebaseAppFactoryModule());
                    modules.add(new ConfigurationModule(() -> properties));
                    modules.add(new AppleIapReceiptInvokerModule());
                    modules.add(new AuthOperationsModule());
                    modules.add(new MetaIapReceiptInvokerModule());
                    modules.add(new ClusterContextFactoryModule());
                    modules.add(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
                    return modules;
                })
                .withUnixFSWorker()
                .withWorkerBindAddress(format("tcp://127.0.0.1:%d", nodePort))
                .withDefaultHttpClient();
    }


}

