package com.namazustudios.socialengine.fts.annotation;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

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

    String DEFAULT_CLASS_XPATH_QUERY = "getClass($document)";

    String DEFAULT_CLASS_FIELD_NAME = "class";

    /**
     * This specifies a way to store the Java fully qualified name for a {@link Class}
     * so it can be used to index the types, or types, that the {@link Document} will
     * contain.s
     *
     * Note that the value of {@link SearchableField#store()} is ignored and
     * will overwritten with a value of {@link org.apache.lucene.document.Field.Store#YES}
     *
     * @return the searchable field representing the type
     */
    SearchableField type() default @SearchableField(
            path = SearchableDocument.DEFAULT_CLASS_XPATH_QUERY,
            name = SearchableDocument.DEFAULT_CLASS_FIELD_NAME,
            type = Class.class);

    /**
     * Used to specify multiple {@link SearchableField} annotations. Which
     * are used to generate a {@link org.apache.lucene.document.Document} from
     * the annotated type.
     */
    SearchableField[] fields() default {};

}
