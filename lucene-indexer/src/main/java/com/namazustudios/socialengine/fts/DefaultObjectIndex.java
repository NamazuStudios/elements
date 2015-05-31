package com.namazustudios.socialengine.fts;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 * Default implementation of {@link ObjectIndex}.  This requires both an instance
 * of {@link IndexSearcher} and {@link IndexWriter} to function.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class DefaultObjectIndex extends AbstractObjectIndex {

    /**
     * Initializes the DefaultObjectIndex with an instance of {@link DefaultDocumentGenerator}
     *
     * @param indexWriterContextProvider the IOContextProvider for the index writer
     * @param indexSearcherContextProvider the IOContextProvider for the index searcher
     */
    public DefaultObjectIndex(final IOContext.Provider<IndexWriter> indexWriterContextProvider,
                              final IOContext.Provider<IndexSearcher> indexSearcherContextProvider) {
        super(new DefaultDocumentGenerator(), indexWriterContextProvider, indexSearcherContextProvider);
    }

    /**
     * Initializes the DefaultObjectIndex with a specific instance of {@link DocumentGenerator}.
     *
     * @param documentGenerator the {@link DocumentGenerator} used by the object index
     * @param indexWriterContextProvider the IOContextProvider for the index writer
     * @param indexSearcherContextProvider the IOContextProvider for the index searcher
     */
    public DefaultObjectIndex(final DocumentGenerator documentGenerator,
                              final IOContext.Provider<IndexWriter> indexWriterContextProvider,
                              final IOContext.Provider<IndexSearcher> indexSearcherContextProvider) {
        super(documentGenerator, indexWriterContextProvider, indexSearcherContextProvider);
    }

}
