package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
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

    private final IOContext<IndexSearcher> indexSearcherIOContext;

    private final ObjectQuery objectQuery;

    private final DocumentGenerator documentGenerator;

    public TopDocsSearchResult(final ObjectQuery objectQuery,
                               final TopDocs topDocs,
                               final IOContext<IndexSearcher> indexSearcherIOContext,
                               final DocumentGenerator documentGenerator) {
        super(objectQuery, documentGenerator, indexSearcherIOContext);
        this.topDocs = topDocs;
        this.objectQuery = objectQuery;
        this.indexSearcherIOContext = indexSearcherIOContext;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public int available() {
        return topDocs.scoreDocs.length;
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

            @Override
            public String toString() {
                return TopDocsSearchResult.class.getName() + "{" +
                        "scoreDocs=" + Arrays.toString(scoreDocs) +
                        ", objectQuery " + objectQuery +
                        '}';
            }

        };
    }

    @Override
    public DocumentEntry<DocumentT> singleResult() {

        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        if (available() == 1) {
            return getEntry(scoreDocs[0].doc);
        } else if (available() == 0) {
            throw new NoResultException();
        } else {
            throw new MultipleResultException();
        }

    }

    protected ScoredDocumentEntry<DocumentT> getEntry(final ScoreDoc scoreDoc) {
        final DocumentEntry<DocumentT> documentEntry = getEntry(scoreDoc.doc);
        return wrap(documentEntry, scoreDoc);
    }

    private <U> ScoredDocumentEntry<U> wrap(final DocumentEntry<U> documentEntry, final ScoreDoc scoreDoc) {

        return new ScoredDocumentEntry<U>() {

            @Override
            public double getScore() {
                return scoreDoc.score;
            }

            @Override
            public Document getDocument() {
                return documentEntry.getDocument();
            }

            @Override
            public Identity<U> getIdentity(Class<U> aClass) {
                return documentEntry.getIdentity(aClass);
            }

            @Override
            public <DocumentSuperT> ScoredDocumentEntry<DocumentSuperT> as(Class<? super U> cls) {
                final DocumentEntry<DocumentSuperT> entry = documentEntry.as(cls);
                return wrap(entry, scoreDoc);
            }

            @Override
            public Fields<U> getFields(Class<U> documentTClassType) {
                return documentEntry.getFields(documentTClassType);
            }

            @Override
            public String toString() {
                return "Delegates to " + documentEntry.toString();
            }

        };
    }

    /**
     * Performs a subsequent query to fetch documents after this query.  It should
     * be noted that the original query must have enough available results in order
     * to find the starting document.  If there is not enough availble, then this
     * will throw an instance of {@link NoResultException}.
     *
     * @param offset the offset, in this result set, to use
     * @param count the number of documents to find
     *
     * @return a new instance with the new results
     *
     * @throws NoResultException if there are not enough availble results  to offset
     */
    public TopDocsSearchResult<DocumentT> after(final int offset, final int count) {

        // Corrects the offset value, ensures it's zero or more.

        if (offset >= topDocs.scoreDocs.length) {
            throw new NoResultException("offset exceeds available documents " + offset);
        }

        if (offset < 0 || count < 0) {
            throw new IllegalArgumentException("offset and count must be positive ");
        } else if (offset == 0) {
            return this;
        }

        final TopDocs newTopDocs;
        final ScoreDoc scoreDoc = topDocs.scoreDocs[offset - 1];

        try {
            newTopDocs = indexSearcherIOContext.instance().searchAfter(scoreDoc, objectQuery.getQuery(), count);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

        return new TopDocsSearchResult<>(objectQuery, newTopDocs, indexSearcherIOContext, documentGenerator);

    }

}
