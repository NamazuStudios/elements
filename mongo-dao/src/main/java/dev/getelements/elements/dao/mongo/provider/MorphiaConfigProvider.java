package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.DiscriminatorFunction;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.List;

public class MorphiaConfigProvider implements Provider<MorphiaConfig> {

    @Inject
    @Named(MongoConfigurationService.DATABASE_NAME)
    private Provider<String> databaseNameProvider;

    @Override
    public MorphiaConfig get() {
        return MorphiaConfig.load()
                .applyIndexes(true)
                .enablePolymorphicQueries(true)
                .discriminator(DiscriminatorFunction.className())
                .packages(List.of("dev.getelements.elements.dao.mongo.*"))
                .database(databaseNameProvider.get());
    }

}
