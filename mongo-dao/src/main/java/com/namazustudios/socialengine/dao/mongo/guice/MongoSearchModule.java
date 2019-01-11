package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.elements.fts.concurrent.Condition;
import com.namazustudios.socialengine.dao.mongo.provider.*;
import com.namazustudios.elements.fts.ObjectIndex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;

/**
 * Sets up and configures the {@link ObjectIndex} using the Mongo Lucene drivers
 * so that the search index can be stored directly in the mongo database.  This
 * includes some configuration parameters as well.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class MongoSearchModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(Analyzer.class).toProvider(MongoStandardAnalyzerProvider.class);
        bind(Directory.class).toProvider(MongoDirectoryProvider.class).asEagerSingleton();
        bind(Condition.class).toProvider(JeroMQConditionProvider.class).asEagerSingleton();
        bind(LockFactory.class).toProvider(MongoLockFactoryProvider.class).asEagerSingleton();
        bind(ObjectIndex.class).toProvider(MongoObjectIndexProvider.class).asEagerSingleton();
        expose(ObjectIndex.class);
    }

}
