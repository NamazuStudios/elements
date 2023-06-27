package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import dev.getelements.elements.dao.mongo.provider.MongoClientProvider;
import dev.getelements.elements.dao.mongo.provider.MongoDatabaseProvider;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoCoreModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().bind(MongoClient.class)
                .toProvider(MongoClientProvider.class)
                .asEagerSingleton();

        binder().bind(MongoDatabase.class)
                .toProvider(MongoDatabaseProvider.class);

    }

}
