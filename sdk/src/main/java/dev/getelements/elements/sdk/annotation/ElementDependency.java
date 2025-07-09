package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BiPredicate;
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
     * The query string used to select the {@link Element}. By default, this is the name of the {@link Element} as
     * defined in its {@link ElementDefinitionRecord#name()}. A custom selector can be specified using the
     * {@link #selector()} and providing custom selection criteria.
     *
     * @return the name of the {@link Element}
     */
    String value();

    /**
     * Specifies a {@link Predicate} type which allows for custom selection of the {@link Element} instances.
     */
    Class<? extends BiPredicate<ElementDependency, Element>> selector() default Named.class;

    /**
     * Selects the default {@link Element} based on the value of {@link ElementDefinitionRecord#name()}.
     */
    class Named implements BiPredicate<ElementDependency, Element> {
        @Override
        public boolean test(final ElementDependency dependency, final Element element) {
            return element.getElementRecord().definition().name().equals(dependency.value());
        }
    }

}
