package com.namazustudios.socialengine.dao.mongo;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.MongoTestModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDozerMapperProvider;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import dev.morphia.Datastore;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.Properties;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static java.lang.String.format;


public class IntegrationTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestModule.class);

    public static final String TEST_COMPONENT = "com.namazustudios.socialengine.dao.mongo.IntegrationTestModule.test";

    private static final int TEST_MONGO_PORT = 45000;

    private static final String TEST_BIND_IP = "localhost";

    @Override
    protected void configure() {
        
        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        install(new ConfigurationModule(() -> {
            final Properties properties = defaultConfigurationSupplier.get();
            properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_BIND_IP, TEST_MONGO_PORT));
            return properties;
        }));

        install(new MongoDaoModule(){
            @Override
            protected void configure() {
                super.configure();
                expose(Datastore.class);
            }
        });

        bind(Mapper.class)
            .annotatedWith(Names.named(TEST_COMPONENT))
            .toProvider(MongoDozerMapperProvider.class);

        bind(UserTestFactory.class).asEagerSingleton();
        bind(ProfileTestFactory.class).asEagerSingleton();
        bind(ApplicationTestFactory.class).asEagerSingleton();

        install(new MongoTestModule());
        install(new MongoCoreModule());
        install(new MongoSearchModule());
        install(new ValidationModule());

    }

}
