package dev.getelements.elements.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the serialization strategy for a model.  In the event that a model needs to be serialized with varying
 * strategies, this can be used to hint the underlying container how it should be serialized or deserialized.  In some
 * cases, it may be necessary to override or change the behavior of the underlying serialization.
 *
 * This only affects the use case when used in a client context.
 *
 * If unspecified, the default scheme will be used.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientSerializationStrategy {

    /**
     * Implies serialized fields follow the Apple/iTunes Style
     */
    String APPLE_ITUNES = "dev.getelements.elements.sdk.model.annotation.SerializationStrategy.apple.itunes";

    /**
     * Implies serialized fields follow the Apple/iTunes Style
     */
    String OCULUS_GRAPH = "dev.getelements.elements.sdk.model.annotation.SerializationStrategy.oculus.graph";

    /**
     * Implies that serialized fields use lowerCamelCase
     */
    String LCAMEL = "dev.getelements.elements.sdk.model.annotation.SerializationStrategy.lcamel";

    /**
     * Indicates the default case which is {@link #LCAMEL}.
     */
    String DEFAULT = LCAMEL;

    /**
     * The name of the serialization driver to use.
     *
     * @return the name of the serialization driver to use.
     */
    String value() default LCAMEL;

}
