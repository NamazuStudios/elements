package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    public static final String DATABASE_NAME = "com.namazustudios.socialengine.mongo.database.name";

    @Inject
    private Provider<MongoClient> mongoProvider;

    @Override
    public Datastore get() {

        final MongoClient mongoClient = mongoProvider.get();

        final Datastore Datastore;
        Datastore = Morphia.createDatastore(mongoClient, "elements");
        Datastore.getMapper().mapPackage("com.namazustudios.socialengine.dao.mongo");
        Datastore.ensureIndexes();

        return Datastore;

    }

}
