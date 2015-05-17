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
     * Initializes a new instance with a {@link DefaultDocumentGenerator} and an instance
     * of {@link IndexWriter} and {@link IndexSearcher}.
     *
     * @param indexWriter the index writer
     * @param indexSearcher the index searcher
     *
     */
    public DefaultObjectIndex(IndexWriter indexWriter, IndexSearcher indexSearcher) {
        this(new DefaultDocumentGenerator(), indexWriter, indexSearcher);
    }

    /**
     * Initializes a new instance with the given {@link DocumentGenerator} and an instance
     * of {@link IndexWriter} and {@link IndexSearcher}.
     *
     * @param indexWriter the index writer
     * @param indexSearcher the index searcher
     *
     */
    public DefaultObjectIndex(DocumentGenerator documentGenerator, IndexWriter indexWriter, IndexSearcher indexSearcher) {
        super(documentGenerator, indexWriter, indexSearcher);
    }

}
