package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDirectoryProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoObjectIndexProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoStandardAnalyzerProvider;
import com.namazustudios.elements.fts.ObjectIndex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import javax.inject.Singleton;

/**
 * Sets up and configures the {@link ObjectIndex} using the Mongo Lucene drivers
 * so that the search index can be stored directly in the mongo database.  This
 * includes some configuration parameters as well.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class MongoSearchModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().bind(Analyzer.class)
                .toProvider(MongoStandardAnalyzerProvider.class);

        binder().bind(Directory.class)
                .toProvider(MongoDirectoryProvider.class)
                .in(Singleton.class);

        binder().bind(ObjectIndex.class)
                .toProvider(MongoObjectIndexProvider.class)
                .in(Singleton.class);

    }

}
