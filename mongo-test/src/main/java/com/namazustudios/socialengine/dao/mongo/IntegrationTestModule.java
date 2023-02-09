package com.namazustudios.socialengine.dao.mongo;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDozerMapperProvider;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.security.SecureRandomPasswordGenerator;
import dev.morphia.Datastore;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;


public class IntegrationTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestModule.class);

    public static final String TEST_COMPONENT = "com.namazustudios.socialengine.dao.mongo.IntegrationTestModule.test";

    private static final String TEST_BIND_IP = "localhost";

    public static final String MONGO_CLIENT_URI = "com.namazustudios.socialengine.mongo.uri";

    private static final AtomicInteger testPort = new AtomicInteger(45000);

    @Override
    protected void configure() {

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();
        final int port = testPort.getAndIncrement();

        install(new ConfigurationModule(() -> {
            final var properties = defaultConfigurationSupplier.get();
            properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_BIND_IP, port));
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

        bind(PasswordGenerator.class)
                .to(SecureRandomPasswordGenerator.class)
                .asEagerSingleton();

        bind(UserTestFactory.class).asEagerSingleton();
        bind(ProfileTestFactory.class).asEagerSingleton();
        bind(ApplicationTestFactory.class).asEagerSingleton();

        install(new MongoTestInstanceModule(port));
        install(new MongoCoreModule());
        install(new MongoSearchModule());
        install(new ValidationModule());

    }

}
