package com.namazustudios.socialengine.appserve;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appserve.guice.JaxRSClientModule;
import com.namazustudios.socialengine.appserve.guice.JeroMQMultiplexerModule;
import com.namazustudios.socialengine.appserve.guice.ServerModule;
import com.namazustudios.socialengine.appserve.guice.ServicesModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import org.apache.bval.guice.ValidationModule;
import org.eclipse.jetty.server.Server;

import static com.namazustudios.socialengine.rt.PersistenceStrategy.getNullPersistence;

public class AppServeMain {

    public static void main(final String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Supplier<List<FacebookPermission>> facebookPermissionListSupplier;
        facebookPermissionListSupplier =  new FacebookBuiltinPermissionsSupplier();

        return createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new MongoCoreModule(),
            new ServerModule(),
            new AppServeFilterModule(),
            new AppServeSecurityModule(),
            new AppServeServicesModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new ZContextModule(),
            new GameOnInvokerModule(),
            new RTFilesystemGitLoaderModule(),
            new RTDaoModule(),
            new RTGitApplicationModule(),
            new AppServeRedissonServicesmodule(),
            new FacebookBuiltinPermissionsModule(facebookPermissionListSupplier),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(PersistenceStrategy.class).toInstance(getNullPersistence());
                }
            },
            new AppleIapReceiptInvokerModule(),
            new JacksonHttpClientModule()
                    .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
                    .withDefaultObjectMapperProvider(() -> {
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                        return objectMapper;
                    }).withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                final DateFormat dateFormat = new AppleDateFormat();
                objectMapper.setDateFormat(dateFormat);
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            })
        ).getInstance(Server.class);

    }

}
