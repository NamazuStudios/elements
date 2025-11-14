package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.Guice;
import com.google.inject.Key;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import org.testng.annotations.Test;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class MongoConfigurationServiceTest {

    @Test
    public void testWithSslEnabledSecure() {

        final var registry = Guice
                .createInjector(new SslEnabledModule(false))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());;
        assertFalse(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslEnabledInsecure() {

        final var registry = Guice
                .createInjector(new SslEnabledModule(true))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertTrue(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslEnabledDefault() {

        final var registry = Guice
                .createInjector(new SslEnabledModule())
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertFalse(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslDisabledExplicit() {

        final var registry = Guice
                .createInjector(new SslDisabledModule(true))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isEmpty());

    }

    @Test
    public void testWithSslDisabledImplicit() {

        final var registry = Guice
                .createInjector(new SslDisabledModule(false))
                .getInstance(Key.get(ElementRegistry.class, named(ROOT)));

        final var mongoConfiguration = registry
                .find("dev.getelements.elements.sdk.mongo")
                .findFirst()
                .get()
                .getServiceLocator()
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isEmpty());

    }

}
