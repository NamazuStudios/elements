package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.dao.mongo.model.application.*;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnRegistration;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnSession;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.match.MongoMatch;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoMission;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuance;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoProgress;
import dev.morphia.AdvancedDatastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;

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

        final MongoClient mongoClient = mongoProvider.get();

        final AdvancedDatastore advancedDatastore;
        advancedDatastore = (AdvancedDatastore) Morphia.createDatastore(mongoClient, databaseName);
        advancedDatastore.getMapper().mapPackage("com.namazustudios.socialengine.dao.mongo");
        advancedDatastore.ensureIndexes();

        return advancedDatastore;

    }

}
