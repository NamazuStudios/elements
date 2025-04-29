package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.annotation.ElementDependencies;
import dev.getelements.elements.sdk.annotation.ElementDependency;
import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Maps to the {@link ElementDependency} type. Indicates that a particular {@link Element} depends on another
 * {@link Element}.
 *
 * @param name the name of the {@link Element}
 * @param selector a {@link Predicate} type which allows for custom selection of the {@link Element} instances
 */
public record ElementDependencyRecord(
        String name,
        Predicate<Element> selector) {

    /**
     * Gets a {@link Stream} of {@link ElementDependencyRecord} instances from the {@link ElementDependency} annotation
     * assigned to the supplied {@link Package}
     * @param aPackage the {@link Package}
     * @return all {@link ElementDefinitionRecord}s associated with the {@link Package}
     */
    public static Stream<ElementDependencyRecord> fromPackage(final Package aPackage) {

        var dependencies = aPackage.getAnnotation(ElementDependencies.class);
        var annotations = Stream.of(aPackage.getAnnotationsByType(ElementDependency.class));

        if (dependencies != null) {
            annotations = Stream.concat(annotations, Stream.of(dependencies.value()));
        }

        return annotations.map(ElementDependencyRecord::from);

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
            return new ElementDependencyRecord(elementDependency.value(), selector);
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            throw new SdkException(e);
        }

    }

}
