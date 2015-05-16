package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

/**
 * Pools some of the common functionality for all instances of {@link SearchResult}.
 *
 * Created by patricktwohig on 5/16/15.
 */
public abstract class AbstractSearchResult<DocumentT, EntryT extends DocumentEntry<DocumentT>>
        implements SearchResult<DocumentT, EntryT> {

    protected final ObjectQuery objectQuery;

    protected final IndexReader indexReader;

    protected final DocumentGenerator documentGenerator;

    public AbstractSearchResult(ObjectQuery objectQuery, DocumentGenerator documentGenerator, IndexReader indexReader) {
        this.objectQuery = objectQuery;
        this.documentGenerator = documentGenerator;
        this.indexReader = indexReader;
    }

    protected DocumentEntry<DocumentT> getEntry(final int doc) {

        final Class<DocumentT> cls = objectQuery.getDocumentType();

        final Document document;

        try {
            document = indexReader.document(doc);
        } catch (IOException ex) {
            throw new SearchException(ex);
        }

        return documentGenerator.entry(cls, document);

    }

}
