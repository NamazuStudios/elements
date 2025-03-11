package dev.getelements.elements.rt.remote.jeromq;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.Test;

import java.io.*;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider.JEROMQ_ALLOW_PLAIN_TRAFFIC;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider.JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class JeroMQSecurityProviderTest {

    private TemporaryFiles temporaryFiles = new TemporaryFiles(JeroMQSecurityProviderTest.class);

    public static final String PEM = "-----BEGIN PUBLIC KEY-----\n" +
            "V79WKoKVD4fyFW11GA3oZw23o1iO5eS7GdzokFwlfjY=\n" +
            "-----END PUBLIC KEY-----\n" +
            "-----BEGIN PRIVATE KEY-----\n" +
            "aMzcleTE9+ZwrKhLcXtp1sjCSZWfMBjwgPPWfHfP3l8=\n" +
            "-----END PRIVATE KEY-----\n";

    @Test
    public void testProviderLoadsPem() throws IOException {

        final var pemFile = temporaryFiles.createTempFile();

        try (var os = new FileOutputStream(pemFile.toFile());
             var writer = new OutputStreamWriter(os, US_ASCII)
        ) {
            writer.write(PEM);
        }

        final var injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(boolean.class)
                        .annotatedWith(named(JEROMQ_ALLOW_PLAIN_TRAFFIC))
                        .toInstance(false);

                bind(String.class)
                        .annotatedWith(named(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE))
                        .toInstance(pemFile.toString());

                bind(JeroMQSecurity.class)
                        .toProvider(JeroMQSecurityProvider.class)
                        .asEagerSingleton();

            }
        });

        final var security = injector.getInstance(JeroMQSecurity.class);

        if (!(security instanceof JeroMQCurveSecurity)) {
            fail("Expected instance of JeroMQCurveSecurity. Got: " + security);
        }

    }

    @Test(expectedExceptions = CreationException.class)
    public void testProviderEnforcesSecurity() {

        final var injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(boolean.class)
                        .annotatedWith(named(JEROMQ_ALLOW_PLAIN_TRAFFIC))
                        .toInstance(false);

                bind(String.class)
                        .annotatedWith(named(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE))
                        .toInstance("");

                bind(JeroMQSecurity.class)
                        .toProvider(JeroMQSecurityProvider.class)
                        .asEagerSingleton();

            }
        });

        injector.getInstance(JeroMQSecurity.class);

    }

    @Test
    public void testProviderExplicitlyAllowsPlain() {

        final var injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(boolean.class)
                        .annotatedWith(named(JEROMQ_ALLOW_PLAIN_TRAFFIC))
                        .toInstance(true);

                bind(String.class)
                        .annotatedWith(named(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE))
                        .toInstance("");

                bind(JeroMQSecurity.class)
                        .toProvider(JeroMQSecurityProvider.class)
                        .asEagerSingleton();

            }
        });

        final var instance = injector.getInstance(JeroMQSecurity.class);
        assertEquals(instance, JeroMQSecurity.DEFAULT);

    }

}
