package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class NullObjectIndex extends DefaultObjectIndex {

    public NullObjectIndex(final IOContext.Provider<IndexWriter> indexWriterContextProvider,
                           final IOContext.Provider<IndexSearcher> indexSearcherContextProvider) {
        super(indexWriterContextProvider, indexSearcherContextProvider);
    }

    @Override
    public <T> void delete(T model) {}

    @Override
    public <T> DocumentEntry<T> index(T model) {
        return new BasicDocumentEntry<T>(new Document());
    }

}
