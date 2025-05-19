package dev.getelements.elements.jetty;

import com.google.inject.AbstractModule;
import dev.getelements.elements.cdnserve.guice.FileSystemCdnGitLoaderModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoElementModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.service.guice.ServicesElementModule;
import dev.getelements.elements.rt.jersey.guice.JerseyHttpClientModule;
import dev.getelements.elements.rt.kryo.guice.KryoPayloadReaderWriterModule;
import dev.getelements.elements.rt.remote.guice.*;
import dev.getelements.elements.rt.remote.jeromq.guice.*;
import dev.getelements.elements.sdk.guice.RootElementRegistryModule;
import dev.getelements.elements.sdk.model.annotation.FacebookPermission;
import dev.getelements.elements.service.guice.AppleIapReceiptInvokerModule;
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

        // Modules required for core web services
        install(new RootElementRegistryModule());
        install(new ConfigurationModule(() -> properties));
        install(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
        install(new MongoCoreModule());
        install(new MongoDaoElementModule());
        install(new ServicesElementModule());
        install(new ValidationModule());
        install(new AppleIapReceiptInvokerModule());
        install(new JerseyHttpClientModule());
        install(new FileSystemCdnGitLoaderModule());

        // Old cluster code which needs to be replaced
        install(new RandomInstanceIdModule());
        install(new InstanceDiscoveryServiceModule(() -> properties));
        install(new ZContextModule());
        install(new JeroMQSecurityModule());
        install(new ClusterContextFactoryModule());
        install(new JeroMQAsyncConnectionServiceModule());
        install(new JeroMQInstanceConnectionServiceModule());
        install(new JeroMQRemoteInvokerModule());
        install(new JeroMQControlClientModule());
        install(new SimpleRemoteInvokerRegistryModule());
        install(new KryoPayloadReaderWriterModule());
        install(new SimpleInstanceModule());

    }

}
