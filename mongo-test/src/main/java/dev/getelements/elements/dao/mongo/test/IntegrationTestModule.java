package dev.getelements.elements.dao.mongo.test;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoGridFSLargeObjectBucketModule;
import dev.getelements.elements.dao.mongo.provider.MongoDozerMapperProvider;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.model.security.PasswordGenerator;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.security.SecureRandomPasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import java.util.concurrent.atomic.AtomicInteger;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static java.lang.String.format;


public class IntegrationTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestModule.class);

    public static final String TEST_COMPONENT = "dev.getelements.elements.dao.mongo.test.IntegrationTestModule.test";

    private static final String TEST_BIND_IP = "localhost";

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
                expose(BooleanQueryParser.class);
            }
        });

        bind(MapperRegistry.class)
            .annotatedWith(named(TEST_COMPONENT))
            .toProvider(MongoDozerMapperProvider.class);

        bind(PasswordGenerator.class)
                .to(SecureRandomPasswordGenerator.class)
                .asEagerSingleton();

        bind(ElementRegistry.class)
                .annotatedWith(named(ROOT))
                .to(Key.get(MutableElementRegistry.class, named(ROOT)));

        bind(MutableElementRegistry.class)
                .annotatedWith(named(ROOT))
                .toInstance(MutableElementRegistry.newDefaultInstance());

        bind(UserTestFactory.class).asEagerSingleton();
        bind(ProfileTestFactory.class).asEagerSingleton();
        bind(ApplicationTestFactory.class).asEagerSingleton();

        install(new MongoTestInstanceModule(port));
        install(new MongoCoreModule());
        install(new MongoGridFSLargeObjectBucketModule());
        install(new ValidationModule());

    }

}
