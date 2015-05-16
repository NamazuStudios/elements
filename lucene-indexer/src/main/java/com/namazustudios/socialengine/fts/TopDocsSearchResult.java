package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by patricktwohig on 5/16/15.
 */
public class TopDocsSearchResult<DocumentT> implements SearchResult<DocumentT> {

    private final ObjectQuery objectQuery;

    private final TopDocs topDocs;

    private final IndexReader indexReader;

    private final DocumentGenerator documentGenerator;

    public TopDocsSearchResult(ObjectQuery objectQuery,
                               TopDocs topDocs,
                               IndexReader indexReader,
                               DocumentGenerator documentGenerator) {
        this.objectQuery = objectQuery;
        this.topDocs = topDocs;
        this.indexReader = indexReader;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public int total() {
        return topDocs.totalHits;
    }

    @Override
    public Iterator<DocumentEntry<DocumentT>> iterator() {
        return new Iterator<DocumentEntry<DocumentT>>() {

            int pos = 0;

            final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            @Override
            public boolean hasNext() {
                return pos < scoreDocs.length;
            }

            @Override
            public DocumentEntry<DocumentT> next() {

                final ScoreDoc scoreDoc = scoreDocs[pos++];
                final Class<DocumentT> cls = objectQuery.getDocumentType();

                final Document document;

                try {
                    document = indexReader.document(scoreDoc.doc);
                } catch (IOException ex) {
                    throw new SearchException(ex);
                }

                return documentGenerator.entry(cls, document);

            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

}
