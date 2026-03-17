package dev.getelements.elements.dao.mongo.test;

import com.google.inject.Guice;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.sdk.mongo.test.SslDisabledModule;
import dev.getelements.elements.sdk.mongo.test.SslEnabledModule;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertFalse;


public class MongoSslSettingsProviderTest {


    @Test
    public void testWithSslEnabledSecure() {

        final var sslSettings = Guice
                .createInjector(new SslEnabledModule(false))
                .getInstance(SslSettings.class);

        assertTrue(sslSettings.isEnabled());
        assertFalse(sslSettings.isInvalidHostNameAllowed());
        assertNotNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslEnabledInsecure() {

        final var sslSettings = Guice
                .createInjector(new SslEnabledModule(true))
                .getInstance(SslSettings.class);

        assertTrue(sslSettings.isEnabled());
        assertTrue(sslSettings.isInvalidHostNameAllowed());
        assertNotNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslEnabledDefault() {

        final var sslSettings = Guice
                .createInjector(new SslEnabledModule())
                .getInstance(SslSettings.class);

        assertTrue(sslSettings.isEnabled());
        assertFalse(sslSettings.isInvalidHostNameAllowed());
        assertNotNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslDisabledExplicit() {

        final var sslSettings = Guice
                .createInjector(new SslDisabledModule(true))
                .getInstance(SslSettings.class);

        assertFalse(sslSettings.isEnabled());
        assertNull(sslSettings.getContext());

    }

    @Test
    public void testWithSslDisabledImplicit() {

        final var sslSettings = Guice
                .createInjector(new SslDisabledModule(false))
                .getInstance(SslSettings.class);

        assertFalse(sslSettings.isEnabled());
        assertNull(sslSettings.getContext());

    }

}
