package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

/**
 *
 * The basic implementation of the {@link Identity} interface.
 *
 * @param <DocumentT>
 *
 */
public class BasicIdentity<DocumentT> extends AbstractHasDocumentType<DocumentT> implements Identity<DocumentT> {

    private final Document document;

    private final IndexableFieldExtractor.Provider provider;

    private final SearchableIdentity searchableIdentity;

    public BasicIdentity(final Class<DocumentT> documentType,
                         final Document document,
                         final IndexableFieldExtractor.Provider provider) {

        super(documentType, document, provider);

        // We need to make sure there's an actual SearchableIdentity field
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
        return "BasicIdentity{" +
                "document=" + document +
                ", provider=" + provider +
                ", searchableIdentity=" + searchableIdentity +
                '}';
    }
}
