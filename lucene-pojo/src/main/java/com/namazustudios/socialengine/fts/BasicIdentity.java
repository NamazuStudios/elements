package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

import java.util.Objects;

/**
 *
 * The basic implementation of the {@link Identity} interface.
 *
 * @param <DocumentT>
 *
 */
public class BasicIdentity<DocumentT> implements Identity<DocumentT> {

    private final Class<? extends DocumentT> documentType;

    private final Document document;

    private final IndexableFieldExtractor.Provider provider;

    private final SearchableDocument searchableDocument;

    private final SearchableIdentity searchableIdentity;

    public BasicIdentity(Class<DocumentT> documentType) {
        this(documentType, new Document());
    }

    public BasicIdentity(Class<DocumentT> documentType, Document document) {
        this(documentType, document, DefaultIndexableFieldExtractorProvider.getInstance());
    }

    public BasicIdentity(Class<DocumentT> documentType, Document document, IndexableFieldExtractor.Provider provider) {

        // First this must check to make sure that the actual Document matches the requested
        // document type.  If this does not match, then we have problems.

        searchableDocument = documentType.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            throw new DocumentException(documentType + " is not annotated with " + SearchableDocument.class);
        }

        final FieldMetadata typeFieldMetadata = new AnnotationFieldMetadata(searchableDocument.type());
        final IndexableFieldExtractor extractor = provider.get(typeFieldMetadata);

        final Class<?> extractedDocumentType;

        try {
            extractedDocumentType = (Class<?>)extractor.extract(document, typeFieldMetadata);
        } catch (ClassCastException ex) {
            throw new DocumentException("document type is of type " + Class.class, ex);
        }

        if (!documentType.isAssignableFrom(extractedDocumentType)) {
            throw new DocumentException("document does not represent type " + documentType);
        }

        this.documentType = (Class<? extends DocumentT>) extractedDocumentType;

        // Secondly, we need to make sure there's an actual SearchableIdentity field
        // somewhere in the type hierarchy.  If there isn't, then we also have problems...

        Class<?> cls = documentType;
        SearchableIdentity searchableIdentity;

        do {

            searchableIdentity = cls.getAnnotation(SearchableIdentity.class);
            cls = cls.getSuperclass();

            if (searchableIdentity != null) {
                break;
            }

        } while(cls != null);

        if (searchableIdentity == null) {
            throw new DocumentException("Cannot find " + SearchableIdentity.class + " anywhere in the type " +
                    "heirarchy for " + documentType);
        }

        this.document = document;
        this.provider = provider;
        this.searchableIdentity = searchableIdentity;

    }

    @Override
    public Class<? extends DocumentT> getDocumentType() {
        return documentType;
    }

    @Override
    public Class<?> getIdentityType() {
        return searchableIdentity.value().type();
    }

    @Override
    public Object getIdentity() {
        final FieldMetadata fieldMetadata = new AnnotationFieldMetadata(searchableIdentity.value());
        final IndexableFieldExtractor<?> indexableFieldExtractor = provider.get(fieldMetadata);
        return indexableFieldExtractor.extract(document, fieldMetadata);
    }

    @Override
    public <IdentityT> IdentityT getIdentity(Class<IdentityT> identityTClass) {

        try {
            final Object identity = getIdentity();
            return identityTClass.cast(identity);
        } catch (ClassCastException ex) {
            throw new DocumentException(ex);
        }

    }

    @Override
    public String toString() {
        return "Identity{" +
                "documentType=" + documentType +
                ", document=" + document +
                ", provider=" + provider +
                ", searchableDocument=" + searchableDocument +
                ", searchableIdentity=" + searchableIdentity +
                '}';
    }

}
