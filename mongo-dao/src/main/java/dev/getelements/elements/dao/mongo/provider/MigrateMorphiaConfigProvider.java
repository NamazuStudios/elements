package dev.getelements.elements.dao.mongo.provider;

import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.DiscriminatorFunction;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.List;

/**
 * Provides the {@link MorphiaConfig} used by the migration tool. Index application is disabled here
 * ({@code applyIndexes(false)}) so that {@link SelfHealingAtomicReferenceDataStoreProvider} can apply
 * indexes itself, healing any conflict between a newly-declared index's options and a stale on-disk index
 * instead of aborting datastore construction.
 */
public class MigrateMorphiaConfigProvider implements Provider<MorphiaConfig> {

    @Inject
    @Named(MongoConfigurationService.DATABASE_NAME)
    private Provider<String> databaseNameProvider;

    @Override
    public MorphiaConfig get() {
        return MorphiaConfig.load()
                .applyIndexes(false)
                .enablePolymorphicQueries(true)
                .discriminator(DiscriminatorFunction.className())
                .packages(List.of("dev.getelements.elements.dao.mongo.*"))
                .database(databaseNameProvider.get());
    }

}
