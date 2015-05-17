package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

/**
 * Represents the Identity of an object, as specified by {@link SearchableIdentity}.  This
 * is composed of three essential elements.
 *
 * <ul>
 *  <li>The concrete {@link Class<?>} representing the document type iself.</li>
 *  <li>The concrete {@link Class<?>} representing the identifier type (eg int, float, String).</li>
 *  <li>The actual identity object representing the object's unique ID.</li>
 * </ul>
 *
 * Created by patricktwohig on 5/15/15.
 */
public class Identity<DocumentT> {

    private final Class<DocumentT> documentType;

    private final Document document;

    private final IndexableFieldExtractor.Provider provider;

    private final SearchableDocument searchableDocument;

    private final SearchableIdentity searchableIdentity;

    public Identity(Class<DocumentT> documentType) {
        this(documentType, new Document());
    }

    public Identity(Class<DocumentT> documentType, Document document) {
        this(documentType, document, DefaultIndexableFieldExtractorProvider.getInstance());
    }

    public Identity(Class<DocumentT> documentType, Document document, IndexableFieldExtractor.Provider provider) {

        searchableDocument = documentType.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            throw new DocumentException(documentType + " is not annotated with " + SearchableDocument.class);
        } else if (!documentType.isAssignableFrom(searchableDocument.type().type())) {
            throw new DocumentException(documentType + " is not compatible with " + searchableDocument.type().type());
        }

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

        this.documentType = documentType;
        this.document = document;
        this.provider = provider;
        this.searchableIdentity = searchableIdentity;

    }

    /**
     * Gets the Document's type.  This corresponds to {@link SearchableDocument#type()}.
     *
     * @return the document's type.
     */
    public Class<DocumentT> getDocumentType() {
        return documentType;
    }

    /**
     * Gets the object's identity type.  This corresponds to {@link SearchableIdentity#value()}.
     *
     * @return the identity type
     */
    public Class<?> getIdentityType() {
        return searchableIdentity.value().type();
    }

    /**
     * Extracts the identity value from the actual underlying {@link Document}.
     *
     * @return the identity of the document.
     */
    public Object getIdentity() {
        final FieldMetadata fieldMetadata = new AnnotationFieldMetadata(searchableIdentity.value());
        final IndexableFieldExtractor<?> indexableFieldExtractor = provider.get(fieldMetadata);
        return indexableFieldExtractor.extract(document, fieldMetadata);
    }

    /**
     * Extracts and checks this object's identity type, returns the identity cast
     * as the proper type.
     *
     * @return this identity, cast as the correctly checked type
     *
     */
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
