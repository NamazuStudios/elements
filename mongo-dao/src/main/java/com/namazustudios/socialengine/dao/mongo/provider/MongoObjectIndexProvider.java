package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.fts.DefaultObjectIndex;
import com.namazustudios.socialengine.fts.ObjectIndex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoObjectIndexProvider implements Provider<ObjectIndex> {

    @Inject
    private Provider<Analyzer> analyzerProvider;

    @Inject
    private Provider<Directory> directoryProvider;

    @Override
    public ObjectIndex get() {
        final Directory directory = directoryProvider.get();

        try {

            final IndexReader indexReader = DirectoryReader.open(directory);
            final IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzerProvider.get());
            final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            return new DefaultObjectIndex(indexWriter, indexSearcher);

        } catch (IOException e) {
            throw new InternalException(e);
        }

    }

}
