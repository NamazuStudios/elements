package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

/**
 * A wrapper around {@link @SearchableField} or {@link SearchableIdentity}, this contains
 * the basic information needed to generate an {@link IndexableField} from an object.
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
     * {@see SearchableField#processor}
     */
    Class<? extends IndexableFieldProcessor> processor();

    /**
     * {@see SearchableField#text}
     */
    boolean text();

    /**
     * {@see SearchableField#store}
     */
    Field.Store store();

}
