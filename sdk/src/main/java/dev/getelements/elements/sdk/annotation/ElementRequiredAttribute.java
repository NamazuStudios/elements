package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a static final non-null field to declare that the attribute is required — i.e., a value must be supplied
 * by the operator at deployment time. Unlike {@link ElementDefaultAttribute}, no default value is provided; the
 * annotation is purely notational and serves to surface required configuration to operators via the management UI.
 *
 * <p>The value of the annotated static field is the attribute name (the key used to retrieve the value from
 * {@link Attributes}). At load time the {@link ElementLoader} scans for all fields bearing this annotation and
 * includes them in the element metadata so that deployment tooling can warn operators when no override has been
 * configured.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementRequiredAttribute {

    /**
     * A human-readable description of the attribute and why it is required.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Whether the attribute value is sensitive (e.g., passwords, API keys). Sensitive required attributes are
     * displayed masked in management UIs.
     *
     * @return true if sensitive, false otherwise
     */
    boolean sensitive() default false;

}
