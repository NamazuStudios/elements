package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.annotations.Entity;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.DiscriminatorFunction;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.List;

import static dev.morphia.Morphia.createDatastore;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    private static final Logger logger = LoggerFactory.getLogger(MongoDatastoreProvider.class);

    @ElementDefaultAttribute(
            value = "elements",
            description = "The name of the MongoDB database to use for Elements data storage."
    )
    public static final String DATABASE_NAME = "dev.getelements.elements.mongo.database.name";

    @Inject
    @Named(MongoDatastoreProvider.DATABASE_NAME)
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
