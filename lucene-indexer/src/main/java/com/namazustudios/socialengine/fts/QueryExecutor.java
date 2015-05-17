package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import javax.print.Doc;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by patricktwohig on 5/16/15.
 */
public class QueryExecutor<DocumentT> {

    private final ObjectQuery<DocumentT> objectQuery;

    private final IndexSearcher indexSearcher;

    private final DocumentGenerator documentGenerator;

    public QueryExecutor(final DocumentGenerator documentGenerator,
                         final IndexSearcher indexSearcher,
                         final ObjectQuery<DocumentT> objectQuery) {
        this.documentGenerator = documentGenerator;
        this.objectQuery = objectQuery;
        this.indexSearcher = indexSearcher;
    }

    /**
     * Uses a {@link TopScoreDocCollector} to execute the query.
     *
     * @param count the offset, or where to start the search
     * @param count the number of documents to return (per page)
     * @return
     */
    public TopDocsSearchResult<DocumentT> withTopScores(int count) {

        final TopDocs topDocs;

        try {
            topDocs = indexSearcher.search(objectQuery.getQuery(), count);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

        return new TopDocsSearchResult<>(objectQuery, topDocs, indexSearcher, documentGenerator);

    }

    @Override
    public String toString() {
        return "QueryExecutor{" +
                "objectQuery=" + objectQuery +
                '}';
    }
}
