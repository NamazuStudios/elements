package dev.getelements.elements.sdk.spi.guice.annotations;

import dev.getelements.elements.sdk.annotation.ElementService;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static dev.getelements.elements.sdk.spi.guice.annotations.GuiceOptions.LoadingStrategy.LEGACY;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Adds options for the Guice loader SPI. This allows an element author control over how the module's loader works
 * and enables/disable certain options making for easier loading.
 *
 * @since 3.9
 */
@Target(PACKAGE)
@Retention(RUNTIME)
public @interface GuiceOptions {

    /**
     * Specifies the loading strategy to be used by this element.
     *
     * @return the {@link LoadingStrategy}
     */
    LoadingStrategy strategy() default LEGACY;

    /**
     * Sets strict validation rules, attempting to raise as many errors at load time as possible. Depends on the
     * loading strategy.
     *
     * @return true, if strict mode is enabled
     */
    boolean strict() default false;

    /**
     * Specifies how the Guice based SPI loads the Element. This here to reduce some of the friction and fagile nature
     * of the original legacy loading system which uses a combination of annotations to expose servies. Guice largely
     * replaces that, but will often times conflict with itself wasting a lot cycles in the process.
     */
    enum LoadingStrategy {

        /**
         * Legacy loader which preserves the 3.8 and prior behavior honoring all SDK annotations.
         */
        LEGACY,

        /**
         * Defers loading exclusively to all {@link GuiceElementModule}s, ignoring bindings that would otherwise be
         * defined in the SDK annotations to avoid doubly defining them. It is still required to export services to
         * other {@link dev.getelements.elements.sdk.Element}s using the {@link ElementService}. Note that with this
         * strategy set, {@link ElementService#implementation()} is ignored.
         */
        GUICE_MODULE_ONLY

    }

}

