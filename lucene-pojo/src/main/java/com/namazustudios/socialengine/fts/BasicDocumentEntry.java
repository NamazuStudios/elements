package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

/**
 * Represents an entry in the index for a specific type, represented by a {@link Document}.
 *
 * Created by patricktwohig on 5/14/15.
 */
public class BasicDocumentEntry<DocumentT> implements DocumentEntry<DocumentT> {

    private final Document document;

    private final IndexableFieldExtractor.Provider provider;

    /**
     * Creates a DocumentEntry from a {@link Document} and the {@link DefaultIndexableFieldExtractor}
     * instance.
     *
     * @param document the document
     */
    public BasicDocumentEntry(final Document document) {
        this(document, DefaultIndexableFieldExtractorProvider.getInstance());
    }

    /**
     * Creates a new Document with a {@link Document} and a custom{@link IndexableFieldExtractor.Provider}
     * instance.
     *
     * @param document the document
     * @param provider the provider
     */
    public BasicDocumentEntry(final Document document, final IndexableFieldExtractor.Provider provider) {
        this.document = document;
        this.provider = provider;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public <DocumentSuperT> DocumentEntry<DocumentSuperT> as(final Class<? super DocumentT> cls) {
        return new BasicDocumentEntry<>(document, provider);
    }

    @Override
    public Identity<DocumentT> getIdentity(final Class<DocumentT> aClass) {
        return new BasicIdentity<>(aClass, document, provider);
    }

    @Override
    public Fields<DocumentT> getFields(Class<DocumentT> documentTClassType) {
        return new BasicFields<>(documentTClassType, document, provider);
    }

    @Override
    public String toString() {
        return "BasicDocumentEntry{" +
                "document=" + document +
                ", provider=" + provider +
                '}';
    }

}
