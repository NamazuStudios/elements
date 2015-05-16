package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Document;

import java.util.Objects;

/**
 * Represents an entry in the index for a specific type, represented by a {@link Document}.
 *
 * Created by patricktwohig on 5/14/15.
 */
public class DocumentEntry<DocumentT> {

    private final Document document;

    private final IndexableFieldExtractor.Provider provider;

    /**
     * Creates a DocumentEntry from a {@link Document} and the {@link DefaultIndexableFieldExtractor}
     * instance.
     *
     * @param document the document
     */
    public DocumentEntry(final Document document) {
        this(document, DefaultIndexableFieldExtractorProvider.getInstance());
    }

    /**
     * Creates a new Document with a {@link Document} and a custom{@link IndexableFieldExtractor.Provider}
     * instance.
     *
     * @param document the document
     * @param provider the provider
     */
    public DocumentEntry(final Document document, final IndexableFieldExtractor.Provider provider) {
        this.document = document;
        this.provider = provider;
    }

    /**
     * Gets the full {@link Document} instance.
     *
     * @return the full {@link Document} instance.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Extracts and generates the {@link Identity} for the encapsulated {@link Document} by reading
     * the annotations on the given class and extracting values from the document.
     *
     * @return the identity for this document
     *
     * @throws DocumentException if there is a problem generating the document's identity
     */

    public Identity<DocumentT> getIdentifier(final Class<DocumentT> aClass) {
        return new Identity<>(aClass, document, provider);
    }

}
