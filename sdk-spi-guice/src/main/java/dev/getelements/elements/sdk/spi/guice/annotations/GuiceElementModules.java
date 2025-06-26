package dev.getelements.elements.sdk.spi.guice.annotations;


import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link Repeatable} annotation for {@link GuiceElementModule}.
 */
@Target(PACKAGE)
@Retention(RUNTIME)
public @interface GuiceElementModules {

    /**
     * Specifies the {@link GuiceElementModule} to be used when creating the Elements.
     * @return the {@link GuiceElementModule} types to be used when creating the element.
     */
    GuiceElementModule[] value();

}
