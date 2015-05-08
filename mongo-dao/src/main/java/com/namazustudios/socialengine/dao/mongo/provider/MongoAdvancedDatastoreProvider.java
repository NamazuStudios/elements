package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.namazustudios.socialengine.dao.mongo.model.*;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.lazy.DatastoreProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoAdvancedDatastoreProvider implements Provider<AdvancedDatastore> {

    @Inject
    @Named(MongoDatastoreProvider.DATABASE_NAME)
    private String databaseName;

    @Inject
    private Provider<MongoClient> mongoProvider;

    @Override
    public AdvancedDatastore get() {

        final MongoClient mongoClient = mongoProvider.get();

        final Morphia morphia = new Morphia();

        morphia.map(
                MongoBasicEntrant.class,
                MongoShortLink.class,
                MongoSocialCampaign.class,
                MongoSteamEntrant.class,
                MongoUser.class
        );

        // There doesn't seem to be a good way to get an instanceof AdvancedDatastore
        // maybe there's more to it than this but we're going with this for now.

        final AdvancedDatastore datastore = (AdvancedDatastore) morphia.createDatastore(mongoClient, databaseName);
        datastore.ensureIndexes();
        return datastore;

    }

}
