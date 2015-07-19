package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.namazustudios.socialengine.dao.mongo.model.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    @Inject
    @Named(MongoDatabaseProvider.DATABASE_NAME)
    private String databaseName;

    @Inject
    private Provider<MongoClient> mongoProvider;

    @Override
    public Datastore get() {

        final Morphia morphia = new Morphia();

        morphia.map(
                MongoBasicEntrant.class,
                MongoShortLink.class,
                MongoSocialCampaign.class,
                MongoSteamEntrant.class,
                MongoUser.class,
                MongoApplication.class
        );


        final MongoClient mongoClient = mongoProvider.get();
        final Datastore datastore = morphia.createDatastore(mongoClient, databaseName);
        datastore.ensureIndexes();
        return datastore;

    }

}
