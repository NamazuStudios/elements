package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.record.ElementRecord;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a particular {@link Element} depends on another {@link Element}.
 */
@Target(PACKAGE)
@Retention(RUNTIME)
@Repeatable(ElementDependencies.class)
public @interface ElementDependency {

    /**
     * The name of the {@link Element}
     * @return the name of the {@link Element}
     */
    String value();

    /**
     * Specifes a {@link Predicate} type which allows for custom selection of the {@link ElementRecord} instances
     * should it be required.
     */
    Class<? extends Predicate<Element>> selector() default DefaultElementSelector.class;

    /**
     * Selects the default {@link Element}
     */
    class DefaultElementSelector implements Predicate<Element> {
        @Override
        public boolean test(Element elementRecord) {
            return true;
        }
    }

}
