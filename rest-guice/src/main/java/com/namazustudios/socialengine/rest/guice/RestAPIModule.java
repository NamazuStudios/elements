package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTDaoModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.dao.rt.guice.RTGitApplicationModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ZContextModule;
import com.namazustudios.socialengine.service.guice.*;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.util.AppleDateFormat;
import org.apache.bval.guice.ValidationModule;

import java.text.DateFormat;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;

public class RestAPIModule extends AbstractModule {

    private final Supplier<Properties> configurationSupplier;

    private final Supplier<List<FacebookPermission>> facebookPermissionSupplier;

    @SuppressWarnings("unused")
    public RestAPIModule() {
        this(new DefaultConfigurationSupplier());
    }

    public RestAPIModule(final Supplier<Properties> propertiesSupplier) {
        this(propertiesSupplier, new FacebookBuiltinPermissionsSupplier());
    }

    public RestAPIModule(final ClassLoader classLoader) {
        this(new DefaultConfigurationSupplier(classLoader), new FacebookBuiltinPermissionsSupplier(classLoader));
    }

    public RestAPIModule(final Supplier<Properties> configurationSupplier,
                         final Supplier<List<FacebookPermission>> facebookPermissionSupplier) {
        this.configurationSupplier = configurationSupplier;
        this.facebookPermissionSupplier = facebookPermissionSupplier;
    }

    @Override
    protected void configure() {

        final Properties properties = configurationSupplier.get();
        final String apiRoot = properties.getProperty(Constants.API_PREFIX);

        install(new ConfigurationModule(() -> properties));
        install(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
        install(new JerseyModule(apiRoot) {
            @Override
            protected void configureResoures() {
                        enableAllResources();
                    }
        });
        install(new RestAPIServicesModule());
        install(new NotificationServiceModule());
        install(new GuiceStandardNotificationFactoryModule());
        install(new FirebaseAppFactoryModule());
        install(new RestAPIRedissonServicesModule());
        install(new RestAPISecurityModule());
        install(new MongoCoreModule());
        install(new MongoDaoModule());
        install(new MongoSearchModule());
        install(new RTFilesystemGitLoaderModule());
        install(new ZContextModule());
        install(new RTDaoModule());
        install(new RTGitApplicationModule());
        install(new ValidationModule());
        install(new GameOnInvokerModule());
        install(new AppleIapReceiptInvokerModule());
        install(new JacksonHttpClientModule()
            .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
            .withDefaultObjectMapperProvider(() -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            })
            .withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                final DateFormat dateFormat = new AppleDateFormat();
                objectMapper.setDateFormat(dateFormat);
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            })
        );

    }
}
