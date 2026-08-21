package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.dao.mongo.migration.Migration;
import dev.getelements.elements.dao.mongo.migration.MigrationRunner;
import dev.getelements.elements.dao.mongo.migration.migrations.BackfillProfileSlotZeroMigration;

/**
 * Registers every known {@link Migration} with Guice so both the standalone {@code migrate} CLI and tests
 * resolve the same set. New migrations are added here as an explicit {@link Multibinder} binding -- no
 * classpath scanning -- so ordering and membership stay visible in one place.
 */
public class MongoMigrationModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MigrationRunner.class);

        final var migrations = Multibinder.newSetBinder(binder(), Migration.class);
        migrations.addBinding().to(BackfillProfileSlotZeroMigration.class);

    }

}
