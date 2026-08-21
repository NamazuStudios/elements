package dev.getelements.elements.migrate;

import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoDatastoreBootstrapModule;
import dev.getelements.elements.dao.mongo.guice.MongoMigrationModule;
import dev.getelements.elements.dao.mongo.migration.MigrationRunner;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.SystemVersion;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkModule;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import ru.vyarus.guice.validator.ValidationModule;

/**
 * Standalone entry point that applies pending {@link MigrationRunner} migrations (or reports their status)
 * against the configured MongoDB instance. Deliberately wires only the Mongo/DAO Guice graph -- no web
 * server, no cluster/JeroMQ wiring -- so it can run as a one-shot ops task, either as a second entry class
 * alongside {@code jettyws} on the {@code elements-jetty-ws} Docker image, or standalone (e.g. from a
 * {@code .deb}/systemd unit with no container to piggyback on).
 */
public class Migrate {

    private static final OptionParser optionParser = new OptionParser();

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Prints Help.")
            .forHelp();

    private static final OptionSpec<Void> statusOptionSpec = optionParser
            .accepts("status", "Lists applied and pending migrations without applying any of them.");

    public static void main(final String[] args) throws Exception {
        try {

            final var options = optionParser.parse(args);

            if (options.has(helpOptionSpec)) {
                optionParser.printHelpOn(System.out);
            } else {
                run(options);
            }

        } catch (OptionException ex) {
            System.out.println(ex.getMessage());
            optionParser.printHelpOn(System.out);
            System.exit(1);
        }
    }

    private static void run(final OptionSet options) {

        SystemVersion.CURRENT.logVersion();

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final var injector = Guice.createInjector(
                new ConfigurationModule(defaultConfigurationSupplier::get, defaultConfigurationSupplier::getExplicitProperties),
                new MongoDatastoreBootstrapModule(),
                new MongoSdkModule(),
                new MongoDaoModule(),
                new MongoMigrationModule(),
                new ValidationModule());

        final var runner = injector.getInstance(MigrationRunner.class);

        if (options.has(statusOptionSpec)) {
            status(runner);
        } else {
            runner.run();
        }

    }

    private static void status(final MigrationRunner runner) {

        final var appliedIds = runner.getAppliedIds();
        final var pending = runner.getPending();

        System.out.println("Applied migrations:");
        appliedIds.stream().sorted().forEach(id -> System.out.println("  " + id));

        System.out.println("Pending migrations:");
        pending.forEach(migration -> System.out.println("  " + migration.getId() + " - " + migration.getDescription()));

    }

}
