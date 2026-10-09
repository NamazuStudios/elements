package dev.getelements.elements.dao.mongo.test;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoGridFSLargeObjectBucketModule;
import dev.getelements.elements.dao.mongo.guice.MongoMigrationModule;
import dev.getelements.elements.dao.mongo.guice.PreDatastoreMigrationModule;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.auth.MongoOAuth2AuthScheme;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcAuthScheme;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoSmartContract;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.metadata.MongoMetadata;
import dev.getelements.elements.dao.mongo.model.mission.MongoMission;
import dev.getelements.elements.dao.mongo.model.mission.MongoSchedule;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.dao.mongo.provider.IndexConflictResolver;
import dev.getelements.elements.dao.mongo.provider.MongoIndexConflictResolver;
import dev.getelements.elements.dao.mongo.provider.MorphiaSelfHealingIndexApplier;
import dev.getelements.elements.dao.mongo.provider.SelfHealingAtomicReferenceDataStoreProvider;
import dev.getelements.elements.dao.mongo.provider.SelfHealingIndexApplier;
import dev.getelements.elements.dao.mongo.provider.SelfHealingMorphiaConfigProvider;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkModule;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.dao.mongo.test.MongoIndexSelfHealingIntegrationTest.TEST_COMPONENT;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Verifies end-to-end that the migration tool's datastore bootstrap applies core entity indexes through
 * {@link SelfHealingAtomicReferenceDataStoreProvider}, healing the exact class of conflict reported in
 * {@code #/issues/132} instead of aborting datastore construction. The server boot path binds the plain
 * {@code MorphiaConfigProvider}/{@code MongoAtomicReferenceDataStoreProvider} pair and does not heal; running
 * {@code migrate} before deploying is what resolves conflicts.
 *
 * <p>The auth-scheme entities ({@code MongoOAuth2AuthScheme}/{@code MongoOidcAuthScheme}) explicitly pin their
 * {@code {name: 1}} unique-sparse index as {@code name_1_sparse}. Prior Elements versions created the same spec
 * under the default {@code name_1}, so applying indexes on such a database used to fail those collections with
 * Mongo error 85 ({@code IndexOptionsConflict}). The bootstrap module seeds that legacy {@code name_1} on the
 * {@code oauth2_auth_scheme} and {@code oidc_auth_scheme} collections before the eager datastore realizes, and
 * asserts the bootstrap drops it and creates {@code name_1_sparse}.</p>
 *
 * <p>It also asserts the healing is surgical: the remaining {@code {name: 1}} unique-sparse entities
 * ({@code MongoApplication}, {@code MongoSmartContract}, and the Style-B {@code @Indexed} entities) auto-name
 * their index the plain {@code name_1} -- the same name legacy databases already carry -- so those
 * collections must be left untouched.</p>
 */
@Guice(modules = MongoIndexSelfHealingIntegrationTest.SelfHealingIndexBootstrapModule.class)
public class MongoIndexSelfHealingIntegrationTest {

    public static final String TEST_COMPONENT = "dev.getelements.elements.dao.mongo.test.MongoIndexSelfHealingIntegrationTest";

    private static final String LEGACY_INDEX_NAME = "name_1";

    private static final String SPARSE_INDEX_NAME = "name_1_sparse";

    private Datastore datastore;

    @Test
    public void testMigrateToolHealsLegacyIndexConflicts() {
        // Seeded with legacy "name_1", these collections' entities pin "name_1_sparse" for the same
        // {name:1} unique-sparse spec; the migrate tool's bootstrap must heal instead of aborting.
        assertIndexState(MongoOAuth2AuthScheme.class, SPARSE_INDEX_NAME, true);
        assertIndexState(MongoOidcAuthScheme.class, SPARSE_INDEX_NAME, true);
    }

    @Test
    public void testMigrateToolLeavesUnchangedIndexesAlone() {
        // These entities auto-name the {name:1} unique-sparse spec the plain "name_1" (no "_sparse"
        // suffix), which is exactly the legacy name seeded below; the bootstrap must leave them as-is.
        assertIndexState(MongoApplication.class, LEGACY_INDEX_NAME, false);
        assertIndexState(MongoSmartContract.class, LEGACY_INDEX_NAME, false);
        assertIndexState(MongoMetadata.class, LEGACY_INDEX_NAME, false);
        assertIndexState(MongoMetadataSpec.class, LEGACY_INDEX_NAME, false);
        assertIndexState(MongoMission.class, LEGACY_INDEX_NAME, false);
        assertIndexState(MongoItem.class, LEGACY_INDEX_NAME, false);
        assertIndexState(MongoSchedule.class, LEGACY_INDEX_NAME, false);
    }

    /**
     * Asserts the {@code {name: 1}} unique-sparse spec on the entity's collection is named
     * {@code expectedName} and, when {@code legacyDropped}, that the divergent {@code name_1} is absent.
     */
    private void assertIndexState(final Class<?> entityClass,
                                  final String expectedName,
                                  final boolean legacyDropped) {

        final var collection = getDatastore().getCollection(entityClass);
        final var key = new Document("name", 1);

        boolean expected = false;
        boolean legacy = false;

        for (final var index : collection.listIndexes()) {
            if (!key.equals(index.get("key"))) {
                continue;
            }
            if (!Boolean.TRUE.equals(index.getBoolean("unique")) ||
                    !Boolean.TRUE.equals(index.getBoolean("sparse"))) {
                continue;
            }
            final var name = index.getString("name");
            expected |= expectedName.equals(name);
            legacy |= LEGACY_INDEX_NAME.equals(name);
        }

        assertTrue(expected, "expected index " + expectedName + " on " + entityClass);
        if (legacyDropped) {
            assertFalse(legacy, "legacy index " + LEGACY_INDEX_NAME + " should have been healed on " + entityClass);
        }

    }

    /**
     * Mirrors {@link IntegrationTestModule} but routes datastore construction through the self-healing
     * providers and seeds the legacy conflicting indexes before the eager {@code AtomicReference<Datastore>}
     * is realized by the injector.
     */
    public static class SelfHealingIndexBootstrapModule extends AbstractModule {

        private static final Logger logger = LoggerFactory.getLogger(SelfHealingIndexBootstrapModule.class);

        private static final AtomicInteger testPort = new AtomicInteger(46000);

        @Override
        protected void configure() {

            final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();
            final int port = testPort.getAndIncrement();
            final String uri = format("mongodb://localhost:%d/?socketTimeoutMS=30000", port);

            // Start MongoDB before building the injector so that eager singletons
            // (e.g. AtomicReference<Datastore>) can connect immediately.
            final var mongoTestInstance = new DockerMongoTestInstance(port);
            mongoTestInstance.start();
            getRuntime().addShutdownHook(new Thread(mongoTestInstance::close));

            // CliMongoTestInstance relies on a docker-exec mongosh to run rs.initiate(), which races
            // mongod startup: if the node is left uninitialized its waitForPrimary never succeeds. Initiate
            // (and await a writable primary) deterministically here before seeding or booting.
            ensureWritablePrimary(uri);

            // Seed the legacy "name_1" indexes BEFORE the eager datastore singleton realizes, emulating a
            // database previously written by an Elements version whose {name:1} unique-sparse declaration
            // (and/or auth-scheme naming) produced the divergent index name.
            seedLegacyIndexes(uri);

            install(new ConfigurationModule(() -> {
                final var properties = defaultConfigurationSupplier.get();
                properties.put(MONGO_CLIENT_URI, uri);
                return properties;
            }));

            bind(MorphiaConfig.class)
                    .toProvider(SelfHealingMorphiaConfigProvider.class);

            bind(new TypeLiteral<AtomicReference<Datastore>>(){})
                    .toProvider(SelfHealingAtomicReferenceDataStoreProvider.class)
                    .asEagerSingleton();

            bind(IndexConflictResolver.class).to(MongoIndexConflictResolver.class);
            bind(SelfHealingIndexApplier.class).to(MorphiaSelfHealingIndexApplier.class);

            install(new PreDatastoreMigrationModule());

            install(new MongoDaoModule());

            bind(dev.getelements.elements.sdk.model.util.MapperRegistry.class)
                    .annotatedWith(named(TEST_COMPONENT))
                    .toProvider(dev.getelements.elements.dao.mongo.provider.MongoDozerMapperProvider.class);

            bind(ElementRegistry.class)
                    .annotatedWith(named(ROOT))
                    .to(Key.get(MutableElementRegistry.class, named(ROOT)));

            bind(MutableElementRegistry.class)
                    .annotatedWith(named(ROOT))
                    .toInstance(MutableElementRegistry.newDefaultInstance());

            install(new MongoSdkModule());
            install(new MongoGridFSLargeObjectBucketModule());
            install(new MongoMigrationModule());
            install(new ValidationModule());

        }

        private void seedLegacyIndexes(final String uri) {

            final var databaseName = new DefaultConfigurationSupplier()
                    .get()
                    .getProperty(MongoConfigurationService.DATABASE_NAME, "elements");

            try (var client = MongoClients.create(uri)) {
                final var database = client.getDatabase(databaseName);
                seed(database.getCollection("application"));
                seed(database.getCollection("oauth2_auth_scheme"));
                seed(database.getCollection("smart_contract"));
                seed(database.getCollection("oidc_auth_scheme"));
            }

        }

        private void seed(final MongoCollection<Document> collection) {
            collection.createIndex(
                    Indexes.ascending("name"),
                    new IndexOptions().name(LEGACY_INDEX_NAME).unique(true).sparse(true)
            );
        }

        /**
         * Waits for the single-node replica set to elect a writable primary, initializing it if the
         * {@code mongosh rs.initiate()} invoked by {@link CliMongoTestInstance} lost the race against mongod
         * startup. Prevents the boot from stalling forever behind an uninitialized node.
         */
        private static void ensureWritablePrimary(final String uri) {

            final var connectionString = format("%s&connectTimeoutMS=2000&serverSelectionTimeoutMS=2000", uri);
            final var deadline = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

            while (System.currentTimeMillis() < deadline) {

                try (var client = MongoClients.create(connectionString)) {

                    final var admin = client.getDatabase("admin");
                    final var hello = admin.runCommand(new BsonDocument("hello", new BsonInt32(1)));

                    if (Boolean.TRUE.equals(hello.get("isWritablePrimary", Boolean.class)) ||
                            Boolean.TRUE.equals(hello.get("ismaster", Boolean.class))) {
                        return;
                    }

                    if ("Does not have a valid replica set config".equals(hello.getString("info"))) {
                        // The node has never been initialized; nothing else can succeed until it is.
                        try {
                            admin.runCommand(new BsonDocument("replSetInitiate", new BsonDocument()));
                        } catch (MongoCommandException ex) {
                            logger.warn("replSetInitiate failed; retrying: {}", ex.getMessage());
                        }
                    }

                } catch (MongoException ex) {
                    logger.warn("Waiting for MongoDB primary: {}", ex.getMessage());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while awaiting MongoDB primary.", ex);
                }

            }

            throw new IllegalStateException("MongoDB on " + uri + " never became a writable primary.");

        }

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(final Datastore datastore) {
        this.datastore = datastore;
    }

}