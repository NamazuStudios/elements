package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigration;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigrationRunner;
import dev.getelements.elements.dao.mongo.migration.migrations.DropLegacyAuthSchemeIndexesMigration;

/**
 * Registers every known {@link PreDatastoreMigration} with Guice, mirroring {@link MongoMigrationModule}'s
 * pattern for {@code Migration} but for migrations that must run before the Morphia {@code Datastore} is
 * constructed. New migrations are added here as an explicit {@link Multibinder} binding — no classpath
 * scanning — so ordering and membership stay visible in one place, and call sites that share a
 * {@code MorphiaConfig} (e.g. {@code MongoAtomicReferenceDataStoreProvider}, {@code MongoElementEntityRegistrar})
 * never need their own migration-specific code — they just inject {@link PreDatastoreMigrationRunner} and
 * call {@code run(...)} before {@code Morphia.createDatastore(...)}.
 *
 * <p>Installed everywhere {@code MongoAtomicReferenceDataStoreProvider} is bound (see
 * {@link MongoDaoElementModule}, {@link MongoDatastoreBootstrapModule}, and the test equivalents), since
 * that provider — and {@code MongoElementEntityRegistrar} — depend on {@link PreDatastoreMigrationRunner}.
 */
public class PreDatastoreMigrationModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(PreDatastoreMigrationRunner.class);

        final var migrations = Multibinder.newSetBinder(binder(), PreDatastoreMigration.class);
        migrations.addBinding().to(DropLegacyAuthSchemeIndexesMigration.class);

    }

}
