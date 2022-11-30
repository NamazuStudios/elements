package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.*;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.jersey.JerseyHttpClientModule;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleInstanceModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.*;
import com.namazustudios.socialengine.service.guice.AppleIapReceiptInvokerModule;
import com.namazustudios.socialengine.service.guice.GuiceStandardNotificationFactoryModule;
import com.namazustudios.socialengine.service.guice.NotificationServiceModule;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;

public class EmbeddedRestAPIModule extends AbstractModule {

    private final Supplier<Properties> configurationSupplier;

    private final Supplier<List<FacebookPermission>> facebookPermissionSupplier;

    public EmbeddedRestAPIModule(final Supplier<Properties> propertiesSupplier) {
        this(propertiesSupplier, new FacebookBuiltinPermissionsSupplier());
    }

    public EmbeddedRestAPIModule(final Supplier<Properties> configurationSupplier,
                                 final Supplier<List<FacebookPermission>> facebookPermissionSupplier) {
        this.configurationSupplier = configurationSupplier;
        this.facebookPermissionSupplier = facebookPermissionSupplier;
    }

    @Override
    protected void configure() {

        final Properties properties = configurationSupplier.get();
        final String apiRoot = properties.getProperty(Constants.API_PREFIX);

        bind(ObjectMapper.class).asEagerSingleton();

        install(new InstanceDiscoveryServiceModule(configurationSupplier));
        install(new ConfigurationModule(() -> properties));
        install(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
        install(new RestAPIJerseyModule(apiRoot));
        install(new StandardServletServicesModule());
        install(new NotificationServiceModule());
        install(new GuiceStandardNotificationFactoryModule());
        install(new FirebaseAppFactoryModule());
        install(new StandardServletRedissonServicesModule());
        install(new StandardServletSecurityModule());
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
        bind(InstanceId.class).toInstance(randomInstanceId());

    }
}
