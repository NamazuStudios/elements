package com.namazustudios.socialengine.dao.mongo.provider;

import com.github.mongoutils.collections.DBObjectSerializer;
import com.github.mongoutils.collections.MongoConcurrentMap;
import com.github.mongoutils.collections.SimpleFieldDBObjectSerializer;
import com.github.mongoutils.lucene.MapDirectory;
import com.github.mongoutils.lucene.MapDirectoryEntry;
import com.github.mongoutils.lucene.MapDirectoryEntrySerializer;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.namazustudios.socialengine.exception.InternalException;
import org.apache.lucene.store.Directory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoDirectoryProvider implements Provider<Directory> {

    public static final String SEARCH_INDEX_COLLECTION = "com.namazustudios.socialengine.mongo.search.index.collection";

    public static final String DIRECTORY_KEY_NAME = "com.namazustudios.socialengine.mongo.search.index.directory.key.name";

    public static final String DIRECTORY_ENTRY_NAME = "com.namazustudios.socialengine.mongo.search.index.directory.entry.name";

    @Inject
    @Named(SEARCH_INDEX_COLLECTION)
    private String searchIndexCollection;

    @Inject
    @Named(MongoDatabaseProvider.DATABASE_NAME)
    private String mongoDatabaseName;

    @Inject
    @Named(DIRECTORY_KEY_NAME)
    private String directoryKeyName;

    @Inject
    @Named(DIRECTORY_ENTRY_NAME)
    private String directoryEntryName;

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Override
    public Directory get() {

        final DBObjectSerializer<String> keySerializer
                = new SimpleFieldDBObjectSerializer<>(directoryKeyName);

        final DBObjectSerializer<MapDirectoryEntry> valueSerializer
                = new MapDirectoryEntrySerializer(directoryEntryName);

        final DB db = mongoClientProvider.get().getDB(mongoDatabaseName);
        final DBCollection dbCollection = db.getCollection(searchIndexCollection);

        final ConcurrentMap<String, MapDirectoryEntry> store =
            new MongoConcurrentMap<>(dbCollection, keySerializer, valueSerializer);

        try {
            return new MapDirectory(store);
        } catch (IOException ex) {
            throw new InternalException("Could not instnatiate search index directoy.", ex);
        }

    }

}
