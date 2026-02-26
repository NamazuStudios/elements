package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.TypeRequest;
import dev.getelements.elements.sdk.annotation.ElementTypeRequest;
import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

/**
 * Maps to the {@link ElementTypeRequest} annotation. Pairs the annotation instance with its instantiated
 * {@link TypeRequest} predicate, making type visibility requests available as a typed record within the
 * {@link ElementRecord}.
 *
 * @param annotation the raw {@link ElementTypeRequest} annotation instance
 * @param request the instantiated {@link TypeRequest} predicate
 */
public record ElementTypeRequestRecord(
        ElementTypeRequest annotation,
        TypeRequest request) {

    /**
     * Tests whether the supplied binary class name is permitted by this request.
     *
     * @param binaryName the binary class name as passed to {@code ClassLoader.loadClass()}
     * @return {@code true} if this request permits the class
     */
    public boolean test(final String binaryName) {
        return request.test(annotation, binaryName);
    }

    /**
     * Creates an {@link ElementTypeRequestRecord} from the supplied {@link ElementTypeRequest} annotation by
     * instantiating the {@link TypeRequest} implementation referenced by {@link ElementTypeRequest#request()}.
     *
     * @param annotation the annotation instance
     * @return a new {@link ElementTypeRequestRecord}
     * @throws SdkException if the {@link TypeRequest} implementation cannot be instantiated
     */
    public static ElementTypeRequestRecord from(final ElementTypeRequest annotation) {
        try {
            final var request = annotation.request().getConstructor().newInstance();
            return new ElementTypeRequestRecord(annotation, request);
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            throw new SdkException(e);
        }
    }

    /**
     * Gets a {@link Stream} of {@link ElementTypeRequestRecord} instances from all {@link ElementTypeRequest}
     * annotations on the supplied {@link Package}.
     *
     * @param pkg the {@link Package} to read annotations from
     * @return all {@link ElementTypeRequestRecord}s declared on the package
     */
    public static Stream<ElementTypeRequestRecord> fromPackage(final Package pkg) {
        return Stream.of(pkg.getAnnotationsByType(ElementTypeRequest.class))
                .map(ElementTypeRequestRecord::from);
    }

}