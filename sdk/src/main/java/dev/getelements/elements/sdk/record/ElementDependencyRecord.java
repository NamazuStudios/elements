package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementDependency;
import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Maps to the {@link ElementDependency} type. Indicates that a particular {@link Element} depends on another
 * {@link Element}.
 *
 * @param dependency the raw {@link ElementDependency} annotation
 * @param selector a {@link Predicate} type which allows for custom selection of the {@link Element} instances
 */
public record ElementDependencyRecord(
        ElementDependency dependency,
        BiPredicate<ElementDependency, Element> selector) {

    /**
     * Finds all {@link Element} instances that match the {@link ElementDependency} annotation
     *
     * @param registry the registry
     * @return a {@link Stream} of {@link Element} instances that match the dependency
     */
    public Stream<Element> findDependencies(final ElementRegistry registry) {
        return registry.stream().filter(e -> selector().test(dependency(), e));
    }

    /**
     * Gets a {@link Stream} of {@link ElementDependencyRecord} instances from the {@link ElementDependency} annotation
     * assigned to the supplied {@link Package}
     * @param aPackage the {@link Package}
     * @return all {@link ElementDefinitionRecord}s associated with the {@link Package}
     */
    public static Stream<ElementDependencyRecord> fromPackage(final Package aPackage) {
        return Stream
                .of(aPackage.getAnnotationsByType(ElementDependency.class))
                .map(ElementDependencyRecord::from);
    }

    /**
     * Creates an {@link ElementDependencyRecord} from the {@link ElementDependency} annotation..
     *
     * @param elementDependency the annotation type
     * @return a new {@link ElementDependencyRecord}
     */
    public static ElementDependencyRecord from(final ElementDependency elementDependency) {

        requireNonNull(elementDependency, "elementDependency");

        try {
            final var selector = elementDependency
                    .selector()
                    .getConstructor()
                    .newInstance();
            return new ElementDependencyRecord(elementDependency, selector);
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            throw new SdkException(e);
        }

    }

}
