package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

/**
 * Represents the fields of a {@link Document} as defined by {@link SearchableField} annotations
 * obtained from the {@link SearchableDocument} annotations.
 *
 * Note, this does not include the fields specified by {@link SearchableIdentity}.  To obtain
 * that information please use the {@link Identity} type obtained from the {@link DocumentEntry}
 *
 * Created by patricktwohig on 5/31/15.
 */
public interface Fields<DocumentT> extends HasDocumentType<DocumentT> {

    /**
     * Gets the {@link FieldMetadata} associated with the given field name.
     *
     * The name is determined by the value specified by {@link SearchableField#name()}.
     *
     * @param fieldName the field name
     * @return the {@link FieldMetadata} for the given field name, never null
     *
     * @throws {@link NoSuchFieldException} if the field does not exist
     */
    FieldMetadata getFieldMetadataForName(final String fieldName);

    /**
     * Extracts the value from the field with the given {@link FieldMetadata}.
     *
     * @param fieldMetadata the field
     * @return the field's value
     */
    Object extract(final FieldMetadata fieldMetadata);

    /**
     * Extracts the value from the field with the given {@link FieldMetadata}.  Additionally,
     * this casts the value to the given type.
     *
     * @param fieldMetadata the field
     * @return the field's value
     *
     * @throws DocumentException if the type is incompatible
     */
    <T> T extract(final Class<T> fieldType, final FieldMetadata fieldMetadata);

    /**
     * Extracts the value from the field with the given name.
     *
     * @param field the field
     * @return the field's value
     */
    Object extract(final String field);

    /**
     * Extracts the value from the field with the given name.  Additionally,
     * this casts the value to the given type.
     *
     * @param field the field
     * @return the field's value
     *
     * @throws DocumentException if the type is incompatible
     */
    <T> T extract(final Class<T> fieldType, final String field);

}
