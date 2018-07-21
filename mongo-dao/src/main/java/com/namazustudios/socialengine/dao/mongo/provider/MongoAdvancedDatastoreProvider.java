package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.dao.mongo.model.application.*;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnRegistration;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Morphia;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoAdvancedDatastoreProvider implements Provider<AdvancedDatastore> {

    @Inject
    @Named(MongoDatabaseProvider.DATABASE_NAME)
    private String databaseName;

    @Inject
    private Provider<MongoClient> mongoProvider;

    @Override
    public AdvancedDatastore get() {

        final Morphia morphia = new Morphia();

        morphia.map(
            MongoApplication.class,
            MongoApplicationConfiguration.class,
            MongoBasicEntrant.class,
            MongoFacebookApplicationConfiguration.class,
            MongoGooglePlayApplicationConfiguration.class,
            MongoIosApplicationConfiguration.class,
            MongoGameOnApplicationConfiguration.class,
            MongoProfile.class,
            MongoPSNApplicationConfiguration.class,
            MongoShortLink.class,
            MongoSocialCampaign.class,
            MongoSteamEntrant.class,
            MongoUser.class,
            MongoMatch.class,
            MongoMatchmakingApplicationConfiguration.class,
            MongoGameOnRegistration.class
        );

        final MongoClient mongoClient = mongoProvider.get();

        final AdvancedDatastore advancedDatastore;
        advancedDatastore = (AdvancedDatastore) morphia.createDatastore(mongoClient, databaseName);
        advancedDatastore.ensureIndexes();

        return advancedDatastore;

    }

}
