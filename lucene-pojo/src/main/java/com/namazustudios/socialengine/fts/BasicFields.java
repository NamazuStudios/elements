package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/31/15.
 */
public class BasicFields<DocumentT> implements Fields<DocumentT> {

    private final Class<DocumentT> documentType;

    public BasicFields(final Class<DocumentT> documentType,
                       final IndexableFieldExtractor.Provider provider) {
        this.documentType = documentType;
    }

    @Override
    public Class<DocumentT> getDocumentType() {
        return documentType;
    }

    @Override
    public Object extract(FieldMetadata field) {
        return null;
    }

    @Override
    public <T> T extract(Class<T> fieldType, FieldMetadata field) {
        return null;
    }

    @Override
    public Object extract(String field) {
        return null;
    }

    @Override
    public <T> T extract(Class<T> fieldType, String field) {
        return null;
    }
}
