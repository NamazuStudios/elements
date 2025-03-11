package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a static final non-null field. When loading an {@link Element}, the {@link ElementLoader} scans for all
 * fields bearing this annotation and will assign the value of this annotation to the default value to populate the
 * {@link Attributes} of the {@link Element} at load time. The value of the static field is to be the name of the
 * attribute.
 *
 * This makes it such that the static constant may be used to retrieve the value from {@link Attributes} and the
 * default value will
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementDefaultAttribute {

    /**
     * The default value of the configuration value.
     *
     * @return the default value
     */
    String value();

}
