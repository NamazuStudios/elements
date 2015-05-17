package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

/**
 * A wrapper around {@link @SearchableField} or {@link SearchableIdentity}, this contains
 * the basic information needed to generate an {@link IndexableField} from an object.
 *
 * This is passed to the various identity generators and extractors allowing the
 * implementation to override values specified in the annotation.
 *
 * Created by patricktwohig on 5/13/15.
 */
public interface FieldMetadata {

    /**
     * {@see {@link SearchableField#path()}}
     */
    String path();

    /**
     * {@see {@link SearchableField#name()}}
     */
    String name();

    /**
     * {@see {@link SearchableField#boost()}}
     */
    float boost();

    /**
     * {@see {@link SearchableField#processors()}}
     */
    Class<? extends IndexableFieldProcessor>[] processors();

    /**
     * {@see {@link SearchableField#extractor()}}
     */
    Class<? extends IndexableFieldExtractor> extractor();

    /**
     * {@see {@link SearchableField#text()}}
     */
    boolean text();

    /**
     * {@see {@link SearchableField#store()}}
     */
    Field.Store store();

    /**
     * {@see {@link SearchableField#type()}}
     */
    Class<?> type();

}
