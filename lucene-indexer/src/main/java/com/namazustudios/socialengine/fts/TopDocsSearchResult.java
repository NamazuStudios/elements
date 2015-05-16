package com.namazustudios.socialengine.fts;

import com.sun.org.apache.xpath.internal.operations.Mult;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link SearchResult} implementation that will fetch all results returned
 * from the a {@link TopDocs} object.
 *
 * Created by patricktwohig on 5/16/15.
 */
public class TopDocsSearchResult<DocumentT> extends AbstractSearchResult<DocumentT, ScoredDocumentEntry<DocumentT>> {

    private static final Logger LOG = LoggerFactory.getLogger(TopDocsSearchResult.class);

    private final TopDocs topDocs;

    private final IndexSearcher indexSearcher;

    public TopDocsSearchResult(ObjectQuery objectQuery,
                               TopDocs topDocs,
                               IndexSearcher indexSearcher,
                               DocumentGenerator documentGenerator) {
        super(objectQuery, documentGenerator, indexSearcher.getIndexReader());
        this.topDocs = topDocs;
        this.indexSearcher = indexSearcher;
    }

    @Override
    public int total() {
        return topDocs.totalHits;
    }

    @Override
    public Iterator<ScoredDocumentEntry<DocumentT>> iterator() {
        return new Iterator<ScoredDocumentEntry<DocumentT>>() {

            int pos = 0;

            final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            @Override
            public boolean hasNext() {
                return pos < scoreDocs.length;
            }

            @Override
            public ScoredDocumentEntry<DocumentT> next() {

                final ScoreDoc scoreDoc;

                try {
                    scoreDoc = scoreDocs[pos++];
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new NoSuchElementException();
                }

                return getEntry(scoreDoc);

            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public DocumentEntry<DocumentT> singleResult() {

        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        if (scoreDocs.length == 1) {
            return getEntry(scoreDocs[0].doc);
        } else if (scoreDocs.length == 0) {
            throw new NoResultException();
        } else {
            throw new MultipleResultException();
        }

    }

    protected ScoredDocumentEntry<DocumentT> getEntry(final ScoreDoc scoreDoc) {
        final DocumentEntry<DocumentT> documentEntry = getEntry(scoreDoc.doc);
        return new ScoredDocumentEntry<DocumentT>() {
            @Override
            public double getScore() {
                return scoreDoc.score;
            }

            @Override
            public Document getDocument() {
                return documentEntry.getDocument();
            }

            @Override
            public Identity<DocumentT> getIdentifier(Class<DocumentT> aClass) {
                return documentEntry.getIdentifier(aClass);
            }

            @Override
            public String toString() {
                return documentEntry.toString();
            }

        };
    }

    /**
     * Performs a subsequent query to fetch documents after this query.
     *
     * @param offset the offset, in this result set, to use
     * @param count the number of documents to find.
     * @return a new instance with the new results
     */
    public TopDocsSearchResult<DocumentT> after(int offset, int count) {

        // Corrects the offset value, ensures it's zero or more.

        if (offset >= topDocs.scoreDocs.length) {
            LOG.warn("Offset exceeds available documents " + offset);
            offset = Math.max(topDocs.scoreDocs.length - 1, 0);
        }

        if (offset < 0 || count < 0) {
            throw new IllegalArgumentException("offset and count must be positive ");
        }

        final TopDocs newTopDocs;
        final ScoreDoc scoreDoc = topDocs.scoreDocs[offset];

        try {
            newTopDocs = indexSearcher.searchAfter(scoreDoc, objectQuery.getQuery(), count);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

        return new TopDocsSearchResult<>(objectQuery, newTopDocs, indexSearcher, documentGenerator);

    }

    @Override
    public String toString() {
        return "TopDocsSearchResult{" +
                "objectQuery=" + objectQuery +
                '}';
    }

}
