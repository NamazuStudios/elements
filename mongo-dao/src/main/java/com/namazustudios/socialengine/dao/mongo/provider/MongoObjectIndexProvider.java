package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.*;
import com.namazustudios.socialengine.exception.InternalException;
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

        final IOContext.Provider<IndexWriter> indexWriterProvider  = () -> {
            final Analyzer analyzer = getAnalyzerProvider().get();
            final Directory directory = getDirectoryProvider().get();
            final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer).setOpenMode(CREATE_OR_APPEND);
            final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            return new DefaultIOContext<>(indexWriter);
        };

        final IOContext.Provider<IndexSearcher> indexSearcherProvider = () -> {
            final Directory directory = getDirectoryProvider().get();
            final IndexReader indexReader = DirectoryReader.open(directory);
            final IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            return IOContext.from(indexSearcher, indexReader);
        };

        // Before anything else, Lucene requires an index be created with the appropriate configuration.  This may
        // collide with other processes attempting start-up as well.

        try (final IOContext<IndexWriter> indexWriterIOContext = indexWriterProvider.get()) {
            indexWriterIOContext.instance().commit();
        } catch (IOException ex) {
            throw new InternalException("Could not create search index.", ex);
        }

        return new DefaultObjectIndex(indexWriterProvider, indexSearcherProvider);

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
