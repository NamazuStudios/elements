package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import javax.print.Doc;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for exeucint a query.  This is intended to be a short-lived object which
 * only lives as long as it needs to in order to read from the index.
 *
 * This implements the {@link AutoCloseable} interface such that it may be used in the
 * try-with-resources block.
 *
 * <em>IMPORTANT: </em> this, or one of the subsequent objects returned by this must be
 * closed or else resource hogging may happen.
 *
 * Created by patricktwohig on 5/16/15.
 */
public class QueryExecutor<DocumentT> implements AutoCloseable, Closeable {

    private final ObjectQuery<DocumentT> objectQuery;

    private final IOContext<IndexSearcher> indexSearcherIOContext;

    private final DocumentGenerator documentGenerator;

    public QueryExecutor(final DocumentGenerator documentGenerator,
                         final IOContext<IndexSearcher> indexSearcherIOContext,
                         final ObjectQuery<DocumentT> objectQuery) {
        this.documentGenerator = documentGenerator;
        this.objectQuery = objectQuery;
        this.indexSearcherIOContext = indexSearcherIOContext;
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
            topDocs = indexSearcherIOContext.instance().search(objectQuery.getQuery(), count);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

        return new TopDocsSearchResult<>(objectQuery, topDocs, indexSearcherIOContext, documentGenerator);

    }

    @Override
    public String toString() {
        return "QueryExecutor{" +
                "objectQuery=" + objectQuery +
                '}';
    }

    @Override
    public void close() throws IOException {
        indexSearcherIOContext.close();
    }

}
