package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.PackageRequest;
import dev.getelements.elements.sdk.annotation.ElementPackageRequest;
import dev.getelements.elements.sdk.annotation.ElementPackageRequests;
import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

/**
 * Maps to the {@link ElementPackageRequest} annotation. Pairs the annotation instance with its instantiated
 * {@link PackageRequest} predicate, making package visibility requests available as a typed record within the
 * {@link ElementRecord}.
 *
 * @param annotation the raw {@link ElementPackageRequest} annotation instance
 * @param request the instantiated {@link PackageRequest} predicate
 */
public record ElementPackageRequestRecord(
        ElementPackageRequest annotation,
        PackageRequest request) {

    /**
     * Tests whether the supplied package name is permitted by this request.
     *
     * @param packageName the name of the package being evaluated
     * @return {@code true} if this request permits all classes in the package
     */
    public boolean test(final String packageName) {
        return request.test(annotation, packageName);
    }

    /**
     * Creates an {@link ElementPackageRequestRecord} from the supplied {@link ElementPackageRequest} annotation by
     * instantiating the {@link PackageRequest} implementation referenced by {@link ElementPackageRequest#request()}.
     *
     * @param annotation the annotation instance
     * @return a new {@link ElementPackageRequestRecord}
     * @throws SdkException if the {@link PackageRequest} implementation cannot be instantiated
     */
    public static ElementPackageRequestRecord from(final ElementPackageRequest annotation) {
        try {
            final var request = annotation.request().getConstructor().newInstance();
            return new ElementPackageRequestRecord(annotation, request);
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            throw new SdkException(e);
        }
    }

    /**
     * Gets a {@link Stream} of {@link ElementPackageRequestRecord} instances from all {@link ElementPackageRequest}
     * annotations on the supplied {@link Package}, including those declared as meta-annotations on other annotations
     * present on the package (one level of indirection).
     *
     * <p>This allows composed annotations to group multiple {@link ElementPackageRequest} declarations
     * into a single annotation, reducing boilerplate in element {@code package-info.java} files.
     *
     * @param pkg the {@link Package} to read annotations from
     * @return all {@link ElementPackageRequestRecord}s declared on the package (directly or via meta-annotation)
     */
    public static Stream<ElementPackageRequestRecord> fromPackage(final Package pkg) {
        final var direct = Stream.of(pkg.getAnnotationsByType(ElementPackageRequest.class));
        final var viaMetaAnnotation = Stream.of(pkg.getAnnotations())
                .filter(a -> a.annotationType() != ElementPackageRequest.class
                        && a.annotationType() != ElementPackageRequests.class)
                .flatMap(a -> Stream.of(a.annotationType().getAnnotationsByType(ElementPackageRequest.class)));
        return Stream.concat(direct, viaMetaAnnotation)
                .map(ElementPackageRequestRecord::from);
    }

}