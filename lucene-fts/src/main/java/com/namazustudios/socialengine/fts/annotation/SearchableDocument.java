package com.namazustudios.socialengine.fts.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Added to a model type to define what fields/properties are added
 * to the search index.
 *
 * Created by patricktwohig on 5/12/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableDocument {

    /**
     * Used to specify multiple {@link SearchableField} annotations. Which
     * are used to generate a {@link org.apache.lucene.document.Document} from
     * the annotated type.
     */
    SearchableField[] value() default {};

}
