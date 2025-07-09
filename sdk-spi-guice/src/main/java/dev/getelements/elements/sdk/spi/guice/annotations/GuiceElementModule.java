package dev.getelements.elements.sdk.spi.guice.annotations;

import com.google.inject.Module;
import dev.getelements.elements.sdk.annotation.ElementDefinition;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used with the {@link ElementDefinition}
 */
@Target(PACKAGE)
@Retention(RUNTIME)
@Repeatable(GuiceElementModules.class)
public @interface GuiceElementModule {

    /**
     * Provides a Guice module to be used when creating the Elements. In addition to the types provided by
     * the {@link ElementDefinition} annotation, this allows the user to specify additional services as Guice modules.
     *
     * @return an array of {@link Module} types to be used when creating the element.
     */
    Class<? extends Module> value();

}
