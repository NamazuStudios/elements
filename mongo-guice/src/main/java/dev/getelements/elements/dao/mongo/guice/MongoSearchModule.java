package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.elements.fts.ObjectIndex;
import dev.getelements.elements.dao.mongo.provider.NullObjectIndexProvider;

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
        // Searching is disabled for now
        bind(ObjectIndex.class).toProvider(NullObjectIndexProvider.class).asEagerSingleton();
        expose(ObjectIndex.class);
    }

}
