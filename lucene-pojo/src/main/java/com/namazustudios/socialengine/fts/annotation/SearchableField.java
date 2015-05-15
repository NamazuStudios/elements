package com.namazustudios.socialengine.fts.annotation;

import com.namazustudios.socialengine.fts.DefaultIndexableFieldExtractor;
import com.namazustudios.socialengine.fts.DefaultIndexableFieldProcessor;
import com.namazustudios.socialengine.fts.IndexableFieldExtractor;
import com.namazustudios.socialengine.fts.IndexableFieldProcessor;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigned to the {@link SearchableDocument} annotation specifying JXPath queries
 * to build a Document.
 *
 * Created by patricktwohig on 5/12/15.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableField {

    /**
     * The default boost fields.  This is derived from the current Lucene docs.
     */
    float DEFAULT_BOOST = 1.0f;

    /**
     * A JXPath Query specifying the path to the field.
     *
     * @return the query path
     */
    String path();

    /**
     * The name of the field.
     *
     * Corresponds to {@link IndexableField#name()}
     *
     * @return the name
     */
    String name();

    /**
     * Used as a hint to designate the Java type of the field.  The specified
     * {@link #processor} may ignore this in favor of its own scheme, but it may
     * be necessary in others.
     *
     * @return
     */
    Class<?> type() default DefaultType.class;

    /**
     * The index-time boost for the field.
     *
     * Corresponds to {@link IndexableField#boost()}
     *
     * @return
     */
    float boost() default DEFAULT_BOOST;

    /**
     * Specifies one or more {@link IndexableFieldProcessor} to convert the the property value to
     * one or more instances of {@link IndexableField}.
     *
     * @return the Class
     */
    Class<? extends IndexableFieldProcessor>[] processors() default DefaultIndexableFieldProcessor.class;

    /**
     * Specifies a custom {@link IndexableFieldProcessor} to convert the {@link IndexableField} property
     * to a Java type.
     *
     * @return the Class
     */
    Class<? extends IndexableFieldExtractor> extractor() default DefaultIndexableFieldExtractor.class;

    /**
     * A hint to the specified {@link IndexableFieldProcessor} as to whether
     * or not to treat {@link CharSequence} types as text or individual strings.  By default,
     * this is false.
     *
     * For a more comprehensive explaination of what this means see {@link org.apache.lucene.document.TextField} and
     * {@link org.apache.lucene.document.StringField}.
     *
     * For non-textual types, this is simply ignored.
     *
     * @return true if to prefer text, false to prefer string
     */
    boolean text() default true;

    /**
     * A hint as to whether or not the field should be stored.  For morre informatoin, see
     * the Javadoc for the {@link org.apache.lucene.document.Field.Store} enum.
     *
     * @return the field store hint, defaults to no
     */
    Field.Store store() default Field.Store.NO;

}
