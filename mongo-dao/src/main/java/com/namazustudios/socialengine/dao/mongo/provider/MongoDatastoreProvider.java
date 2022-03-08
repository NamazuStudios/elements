package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.annotations.Entity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    private static final Logger logger = LoggerFactory.getLogger(MongoDatastoreProvider.class);

    public static final String DATABASE_NAME = "com.namazustudios.socialengine.mongo.database.name";

    @Inject
    private Provider<MongoClient> mongoProvider;

    @Override
    public Datastore get() {

        final var client = mongoProvider.get();
        final var datastore = Morphia.createDatastore(client, "elements");

        new Reflections("com.namazustudios.socialengine.dao.mongo")
            .getTypesAnnotatedWith(Entity.class)
            .forEach(datastore.getMapper()::map);

        try {
            datastore.ensureIndexes();
        } catch (MongoCommandException ex) {
            logger.error("Could not recreate indexes.", ex);
        }

        return datastore;

    }


}
