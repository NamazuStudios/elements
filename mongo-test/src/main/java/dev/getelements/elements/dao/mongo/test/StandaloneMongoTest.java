package dev.getelements.elements.dao.mongo.test;

import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;
import ru.vyarus.guice.validator.ValidationModule;

import static java.lang.Thread.sleep;

public class StandaloneMongoTest {

    public static void main(String[] args) throws InterruptedException {

        final var supplier = new DefaultConfigurationSupplier();

        final var injector = Guice.createInjector(
            new ConfigurationModule(supplier),
            new ValidationModule(),
            new MongoDaoModule(),
            new MongoCoreModule(),
            new MongoTestInstanceModule(27017, true)
        );

        try (var instance = injector.getInstance(MongoTestInstance.class)) {
            while (!Thread.interrupted()) {
                sleep(1000);
            }
        }

    }

}
