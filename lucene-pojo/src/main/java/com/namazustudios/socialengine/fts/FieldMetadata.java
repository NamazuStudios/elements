package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Field;

/**
 * A wrapper around {@link @SearchableField}.
 *
 * Created by patricktwohig on 5/13/15.
 */
public interface FieldMetadata {

    /**
     * {@see SearchableField#path}
     */
    String path();

    /**
     * {@see SearchableField#name}
     */
    String name();

    /**
     * {@see SearchableField#boost}
     */
    float boost();

    /**
     * {@see SearchableField#converter}
     */
    Class<? extends IndexableFieldConverter> converter();

    /**
     * {@see SearchableField#text}
     */
    boolean text();

    /**
     * {@see SearchableField#store}
     */
    Field.Store store();

}
