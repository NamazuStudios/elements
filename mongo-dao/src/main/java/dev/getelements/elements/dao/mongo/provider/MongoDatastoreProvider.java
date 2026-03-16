package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.sdk.mongo.provider.MongoDatabaseProvider;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.DiscriminatorFunction;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.List;

import static dev.morphia.Morphia.createDatastore;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    public static final String MAIN = "dev.getelements.elements.mongo.datastore.main";

    @Inject
    @Named(MongoDatabaseProvider.DATABASE_NAME)
    private Provider<String> databaseNameProvider;

    @Inject
    private Provider<MongoClient> mongoProvider;

    @Override
    public Datastore get() {

        final var config = MorphiaConfig.load()
                .applyIndexes(true)
                .enablePolymorphicQueries(true)
                .discriminator(DiscriminatorFunction.className())
                .packages(List.of("dev.getelements.elements.dao.mongo.*"))
                .database(databaseNameProvider.get());

        final var client = mongoProvider.get();
        return createDatastore(client, config);

    }


}
