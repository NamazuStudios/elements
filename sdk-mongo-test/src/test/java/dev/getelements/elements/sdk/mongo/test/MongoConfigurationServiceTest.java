package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.Guice;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import org.testng.annotations.Test;

import static dev.getelements.elements.sdk.mongo.test.MongoConfigOnlyModule.Mode.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class MongoConfigurationServiceTest {

    @Test
    public void testWithSslEnabledSecure() {

        final var mongoConfiguration = Guice
                .createInjector(new MongoConfigOnlyModule(SSL_ENABLED, false))
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertFalse(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslEnabledInsecure() {

        final var mongoConfiguration = Guice
                .createInjector(new MongoConfigOnlyModule(SSL_ENABLED, true))
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertTrue(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslEnabledDefault() {

        final var mongoConfiguration = Guice
                .createInjector(new MongoConfigOnlyModule(SSL_ENABLED))
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isPresent());
        assertFalse(mongoConfiguration.sslConfiguration().sslInvalidHostNamesAllowed());

    }

    @Test
    public void testWithSslDisabledExplicit() {

        final var mongoConfiguration = Guice
                .createInjector(new MongoConfigOnlyModule(SSL_DISABLED_EXPLICIT))
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isEmpty());

    }

    @Test
    public void testWithSslDisabledImplicit() {

        final var mongoConfiguration = Guice
                .createInjector(new MongoConfigOnlyModule(SSL_DISABLED_IMPLICIT))
                .getInstance(MongoConfigurationService.class)
                .getMongoConfiguration();

        assertTrue(mongoConfiguration.findSslConfiguration().isEmpty());

    }

}