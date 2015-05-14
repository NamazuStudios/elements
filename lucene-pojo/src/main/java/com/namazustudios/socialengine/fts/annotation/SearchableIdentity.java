package com.namazustudios.socialengine.fts.annotation;

import com.namazustudios.socialengine.fts.DefaultIndexableFieldConverter;
import com.namazustudios.socialengine.fts.IndexableFieldConverter;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

/**
 * Created by patricktwohig on 5/14/15.
 */
public @interface SearchableIdentity {

    String CLASS_FIELD_NAME = "class";

    /**
     * A JXPath Query specifying the path to the field.
     *
     * @return the query path
     */
    String path();

    /**
     * Specifies a custom {@link IndexableFieldConverter} to convert the the property value to an instance
     * of {@link IndexableField}.
     *
     * @return the Class
     */
    Class<? extends IndexableFieldConverter> converter() default DefaultIndexableFieldConverter.class;

}
