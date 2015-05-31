package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.fts.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.UUID;

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

        final IOContext.Provider<IndexWriter> indexWriterProvider  = new IOContext.Provider<IndexWriter>() {
            @Override
            public IOContext get() throws IOException {

                final Directory directory = directoryProvider.get();

                final IndexWriterConfig indexWriterConfig =
                        new IndexWriterConfig(analyzerProvider.get())
                                .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

                final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
                indexWriter.commit();

                return new DefaultIOContext<>(indexWriter);

            }
        };

        final IOContext.Provider<IndexSearcher> indexSearcherProvider = new IOContext.Provider<IndexSearcher>() {
            @Override
            public IOContext<IndexSearcher> get() throws IOException {

                final Directory directory = directoryProvider.get();
                final IndexReader indexReader = DirectoryReader.open(directory);
                final IndexSearcher indexSearcher = new IndexSearcher(indexReader);

                return new AbstractIOContext<IndexSearcher>(indexSearcher) {
                    @Override
                    protected void doClose() throws IOException {
                        indexReader.close();
                    }
                };

            }
        };

        return new DefaultObjectIndex(indexWriterProvider, indexSearcherProvider);

    }

}
