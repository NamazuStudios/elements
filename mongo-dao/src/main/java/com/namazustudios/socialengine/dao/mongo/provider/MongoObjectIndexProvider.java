package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.*;
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

import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;

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
        // TODO Fix performacne bottlenecks with search index
        return new NullObjectIndex(null, null);
    }

    public Provider<Analyzer> getAnalyzerProvider() {
        return analyzerProvider;
    }

    @Inject
    public void setAnalyzerProvider(Provider<Analyzer> analyzerProvider) {
        this.analyzerProvider = analyzerProvider;
    }

    public Provider<Directory> getDirectoryProvider() {
        return directoryProvider;
    }

    @Inject
    public void setDirectoryProvider(Provider<Directory> directoryProvider) {
        this.directoryProvider = directoryProvider;
    }

}
