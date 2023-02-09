package com.namazustudios.socialengine.rest;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.formidium.FormidiumAppProvider;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.jetty.DynamicMultiAppServerProvider;
import com.namazustudios.socialengine.jetty.ServletContextHandlerProvider;
import com.namazustudios.socialengine.rpc.BlockchainNetworkRpcAppProvider;
import com.namazustudios.socialengine.rpc.ElementsRpcAppProvider;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jersey.JerseyHttpClientModule;
import com.namazustudios.socialengine.rt.remote.guice.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.*;
import com.namazustudios.socialengine.service.guice.AppleIapReceiptInvokerModule;
import com.namazustudios.socialengine.service.guice.GuiceStandardNotificationFactoryModule;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

public class RestAPIServerModule extends AbstractModule {

    private final Supplier<Properties> configurationSupplier;

    private final Supplier<List<FacebookPermission>> facebookPermissionSupplier;

    public RestAPIServerModule(final Supplier<Properties> propertiesSupplier) {
        this(propertiesSupplier, new FacebookBuiltinPermissionsSupplier());
    }

    public RestAPIServerModule(final Supplier<Properties> configurationSupplier,
                               final Supplier<List<FacebookPermission>> facebookPermissionSupplier) {
        this.configurationSupplier = configurationSupplier;
        this.facebookPermissionSupplier = facebookPermissionSupplier;
    }

    @Override
    protected void configure() {

        bind(Server.class).toProvider(DynamicMultiAppServerProvider.class);
        bind(ServletContextHandler.class).toProvider(ServletContextHandlerProvider.class);

        final Properties properties = configurationSupplier.get();
//        bind(ObjectMapper.class).asEagerSingleton();

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

        var apps = Multibinder.newSetBinder(binder(), AppProvider.class);
        apps.addBinding().to(ElementsRpcAppProvider.class);
        apps.addBinding().to(RestAPIAppProvider.class);
        apps.addBinding().to(BlockchainNetworkRpcAppProvider.class);
        apps.addBinding().to(FormidiumAppProvider.class);

    }

}
