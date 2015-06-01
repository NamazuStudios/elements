package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.lucene.document.Document;

import javax.print.Doc;

/**
 * Created by patricktwohig on 5/31/15.
 */
public class AbstractHasDocumentType<DocumentT> implements HasDocumentType<DocumentT> {

    private final Class<? extends DocumentT> documentType;

    private final SearchableDocument searchableDocument;

    public AbstractHasDocumentType(final Class<DocumentT> documentType,
                                   final Document document,
                                   final IndexableFieldExtractor.Provider provider) {

        // This must check to make sure that the actual Document matches the requested
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

    }

    @Override
    public Class<? extends DocumentT> getDocumentType() {
        return documentType;
    }

    @Override
    public SearchableDocument getSearchableDocument() {
        return searchableDocument;
    }

    @Override
    public String toString() {
        return "AbstractHasDocumentType{" +
                "documentType=" + documentType +
                ", searchableDocument=" + searchableDocument +
                '}';
    }

}
