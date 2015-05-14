package com.namazustudios.socialengine.fts.annotation;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a pair of {@link SearchableField} annotations which define the Document's
 * identity.  The {@link Document} will retain some {@link IndexableField}
 *
 * Created by patricktwohig on 5/14/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableIdentity {

    /**
     * Used to specify a {@link SearchableField} which will be the object's
     * unique identity.
     *
     * Note that the value of {@link SearchableField#store()} is ignored and
     * will overwritten with a value of {@link org.apache.lucene.document.Field.Store#YES}
     *
     * @return the SearchableIdentity value
     */
    SearchableField value();

}
