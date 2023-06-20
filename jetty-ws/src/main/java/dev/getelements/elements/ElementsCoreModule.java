package dev.getelements.elements;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.annotation.FacebookPermission;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
import dev.getelements.elements.guice.*;
import dev.getelements.elements.jetty.DynamicMultiAppServerProvider;
import dev.getelements.elements.jetty.ServletContextHandlerProvider;
import dev.getelements.elements.rt.fst.FSTPayloadReaderWriterModule;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.remote.guice.*;
import dev.getelements.elements.rt.remote.jeromq.guice.*;
import dev.getelements.elements.service.guice.AppleIapReceiptInvokerModule;
import dev.getelements.elements.service.guice.GuiceStandardNotificationFactoryModule;
import dev.getelements.elements.service.guice.RedissonServicesModule;
import dev.getelements.elements.service.guice.ServicesModule;
import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

public class ElementsCoreModule extends AbstractModule {

    private final Supplier<Properties> configurationSupplier;

    private final Supplier<List<FacebookPermission>> facebookPermissionSupplier;

    public ElementsCoreModule() {
        this(new DefaultConfigurationSupplier());
    }

    public ElementsCoreModule(final Supplier<Properties> propertiesSupplier) {
        this(propertiesSupplier, new FacebookBuiltinPermissionsSupplier());
    }

    public ElementsCoreModule(final Supplier<Properties> configurationSupplier,
                              final Supplier<List<FacebookPermission>> facebookPermissionSupplier) {
        this.configurationSupplier = configurationSupplier;
        this.facebookPermissionSupplier = facebookPermissionSupplier;
    }

    @Override
    protected void configure() {

        final Properties properties = configurationSupplier.get();

        install(new ConfigurationModule(() -> properties));
        install(new InstanceDiscoveryServiceModule(() -> properties));
        install(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
        install(new GuiceStandardNotificationFactoryModule());
        install(new FirebaseAppFactoryModule());
        install(new MongoCoreModule());
        install(new MongoDaoModule());
        install(new MongoSearchModule());
        install(new ZContextModule());
        install(new ClusterContextFactoryModule());
        install(new ValidationModule());
        install(new AppleIapReceiptInvokerModule());
        install(new JeroMQAsyncConnectionServiceModule());
        install(new JeroMQInstanceConnectionServiceModule());
        install(new JeroMQRemoteInvokerModule());
        install(new JeroMQControlClientModule());
        install(new SimpleRemoteInvokerRegistryModule());
        install(new SimpleInstanceModule());
        install(new FSTPayloadReaderWriterModule());
        install(new JerseyHttpClientModule());
        install(new RandomInstanceIdModule());

        bind(Server.class).toProvider(DynamicMultiAppServerProvider.class);
        bind(ServletContextHandler.class).toProvider(ServletContextHandlerProvider.class);

    }

}
