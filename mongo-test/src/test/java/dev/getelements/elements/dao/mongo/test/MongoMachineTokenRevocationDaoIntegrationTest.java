package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoDatastoreBootstrapModule;
import dev.getelements.elements.dao.mongo.guice.MongoMigrationModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.dao.MachineTokenRevocationDao;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkModule;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import jakarta.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Verifies the machine-token revocation denylist: revocations are recorded, are idempotent, and are scoped
 * to the exact token id.
 */
@Guice(modules = MongoMachineTokenRevocationDaoIntegrationTest.TestModule.class)
public class MongoMachineTokenRevocationDaoIntegrationTest {

    private static final AtomicInteger testPort = new AtomicInteger(46500);

    @Inject
    private MachineTokenRevocationDao machineTokenRevocationDao;

    @Test
    public void testUnknownTokenIsNotRevoked() {
        assertFalse(getMachineTokenRevocationDao().isRevoked("never-revoked"));
    }

    @Test
    public void testRevokedTokenStaysRevokedUntilExpiry() {

        final var expiry = new Timestamp(System.currentTimeMillis() + 600_000);
        getMachineTokenRevocationDao().revoke("revoked-live", expiry);

        assertTrue(getMachineTokenRevocationDao().isRevoked("revoked-live"));

        // Revoking again is idempotent.
        getMachineTokenRevocationDao().revoke("revoked-live", expiry);
        assertTrue(getMachineTokenRevocationDao().isRevoked("revoked-live"));

        // A different token id is unaffected.
        assertFalse(getMachineTokenRevocationDao().isRevoked("revoked-other"));

    }

    @Test
    public void testExpiredRevocationIsNotHonored() {

        final var expiry = new Timestamp(System.currentTimeMillis() - 60_000);
        getMachineTokenRevocationDao().revoke("revoked-expired", expiry);

        assertFalse(getMachineTokenRevocationDao().isRevoked("revoked-expired"));

    }

    public MachineTokenRevocationDao getMachineTokenRevocationDao() {
        return machineTokenRevocationDao;
    }

    public static class TestModule extends com.google.inject.AbstractModule {

        @Override
        protected void configure() {

            final var defaultConfigurationSupplier =
                    new dev.getelements.elements.config.DefaultConfigurationSupplier();
            final int port = testPort.getAndIncrement();
            final String uri = format("mongodb://localhost:%d", port);

            final var mongoTestInstance = new DockerMongoTestInstance(port);
            mongoTestInstance.start();
            java.lang.Runtime.getRuntime().addShutdownHook(new Thread(mongoTestInstance::close));

            install(new ConfigurationModule(() -> {
                final var properties = defaultConfigurationSupplier.get();
                properties.put(MongoConfigurationService.MONGO_CLIENT_URI, uri);
                return properties;
            }));

            install(new MongoDatastoreBootstrapModule());
            install(new MongoSdkModule());
            install(new MongoDaoModule());
            install(new MongoMigrationModule());
            install(new ValidationModule());

        }

    }

}
