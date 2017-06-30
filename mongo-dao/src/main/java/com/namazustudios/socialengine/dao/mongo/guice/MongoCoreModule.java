package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.mongodb.MongoClient;
import com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoCoreModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().bind(MongoClient.class)
                .toProvider(MongoClientProvider.class)
                .asEagerSingleton();
    }

}
