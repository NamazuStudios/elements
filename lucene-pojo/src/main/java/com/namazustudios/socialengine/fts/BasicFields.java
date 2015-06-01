package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by patricktwohig on 5/31/15.
 */
public class BasicFields<DocumentT> extends AbstractHasDocumentType<DocumentT> implements Fields<DocumentT> {

    private final Document document;

    private final IndexableFieldExtractor.Provider provider;

    private final Map<String, FieldMetadata> fieldMetadataMap = new HashMap<>();

    public BasicFields(final Class<DocumentT> documentType,
                       final Document document,
                       final IndexableFieldExtractor.Provider provider) {
        super(documentType, document, provider);

        this.document = document;
        this.provider = provider;

        for (final SearchableField searchableField : getSearchableDocument().fields()) {
            fieldMetadataMap.put(searchableField.name(), new AnnotationFieldMetadata(searchableField));
        }

    }

    @Override
    public FieldMetadata getFieldMetadataForName(String fieldName) {

        final FieldMetadata fieldMetadata = fieldMetadataMap.get(fieldName);

        if (fieldMetadata == null) {
            throw new DocumentException("no such field " + fieldName + " found on " + getSearchableDocument());
        }

        return fieldMetadata;

    }

    @Override
    public Object extract(FieldMetadata fieldMetadata) {
        final IndexableFieldExtractor indexableFieldExtractor = provider.get(fieldMetadata);
        indexableFieldExtractor.extract(document, fieldMetadata);
        return indexableFieldExtractor.extract(document, fieldMetadata);
    }

    @Override
    public <T> T extract(Class<T> fieldType, FieldMetadata fieldMetadata) {
        try {
            final Object out = extract(fieldMetadata);
            return fieldType.cast(out);
        } catch (ClassCastException ex) {
            throw new DocumentException(ex);
        }
    }

    @Override
    public Object extract(String field) {
        final FieldMetadata fieldMetadata = getFieldMetadataForName(field);
        return extract(fieldMetadata);
    }

    @Override
    public <T> T extract(Class<T> fieldType, String field) {
        final FieldMetadata fieldMetadata = getFieldMetadataForName(field);
        return extract(fieldType, fieldMetadata);
    }

}
