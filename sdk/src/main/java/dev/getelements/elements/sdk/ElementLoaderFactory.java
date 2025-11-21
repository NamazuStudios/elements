package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceRecord;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The {@link ElementLoader} factory. This loads an instance of a {@link Element} on-demand. When loading an
 * {@link ElementType#ISOLATED_CLASSPATH} {@link Element}, must supply a {@link ClassLoader} which is parented to
 **/
public interface ElementLoaderFactory {

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the bootstrap classpath with a filtered version of the supplied
     * base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoader(
            Attributes attributes,
            ClassLoaderConstructor classLoaderCtor) {
        return getIsolatedLoader(
                attributes,
                getClass().getClassLoader(),
                classLoaderCtor,
                r -> true);
    }

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the bootstrap classpath with a filtered version of the supplied
     * base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader}
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoader(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor) {
        return getIsolatedLoader(
                attributes,
                baseClassLoader,
                classLoaderCtor,
                r -> true);
    }

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition} matched by the supplied {@link Predicate}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the bootstrap classpath with a filtered version of the supplied
     * base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @param selector a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoader(
            Attributes attributes,
            ClassLoaderConstructor classLoaderCtor,
            Predicate<ElementDefinitionRecord> selector) {
        return getIsolatedLoader(
                attributes,
                getClass().getClassLoader(),
                classLoaderCtor,
                selector);
    }

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition} with the supplied {@link Predicate}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the bootstrap classpath with a filtered version of the supplied
     * base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader}, this will be ultimately be the parent for {@link Element}'s
     *                        {@link ClassLoader} instance.
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @param selector a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoader(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor,
            Predicate<ElementDefinitionRecord> selector) {
        return getIsolatedLoaderWithParent(
                attributes,
                baseClassLoader,
                classLoaderCtor,
                null,
                selector
        );
    }

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition} with the supplied {@link Predicate}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the {@link Element}'s classpath with a filtered version of the
     * supplied base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader}, this will be ultimately be the parent for {@link Element}'s
     *                        {@link ClassLoader} instance.
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoaderWithParent(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor,
            Element element) {
        return getIsolatedLoaderWithParent(
                attributes,
                baseClassLoader,
                classLoaderCtor,
                element,
                r -> true
        );
    }

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition} with the supplied {@link Predicate}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the {@link Element}'s classpath with a filtered version of the
     * supplied base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader}, this will be ultimately be the parent for {@link Element}'s
     *                        {@link ClassLoader} instance.
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @param element the parent {@link Element} to use for the classloader isolation, may be null indicating that 
     *                isolation should exist for the bootstrap classpath only.
     * @param selector a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoaderWithParent(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor,
            Element element,
            Predicate<ElementDefinitionRecord> selector) {

        final var parent = Optional.ofNullable(element)
                .map(Element::getElementRecord)
                .map(ElementRecord::classLoader)
                .orElse(null);

        return getIsolatedLoaderWithParent(
                attributes,
                baseClassLoader,
                classLoaderCtor,
                parent,
                selector
        );

    }

    /**
     * <p>
     * Scans the classpath, using the supplied {@link ClassLoader}, for {@link Element} instances. If the element is
     * found, then this returns an instance of {@link ElementLoader} which can be used to instantiate the
     * {@link Element}. With the supplied {@link ClassLoader} (from the supplied {@link Function} there must exist
     * exactly one {@link ElementDefinition} with the supplied {@link Predicate}.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#ISOLATED_CLASSPATH} {@link Element}
     * </p>
     *
     * The returned {@link ElementLoader} will use the {@link Element}'s classpath with a filtered version of the
     * supplied base {@link ClassLoader} as the parent classloader.
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader}, this will be ultimately be the parent for {@link Element}'s
     *                        {@link ClassLoader} instance.
     * @param classLoaderCtor the {@link ClassLoader} classloader
     * @param parent the parent {@link ClassLoader} to use for the classloader isolation, may be null indicating that
     *                isolation should exist for the bootstrap classpath only.
     * @param selector a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementLoader}
     * @since 3.6
     */
    ElementLoader getIsolatedLoaderWithParent(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor,
            ClassLoader parent,
            Predicate<ElementDefinitionRecord> selector);

    /**
     * <p>
     * Scans the supplied {@link Package} for {@link Element} instances with the supplied {@link Package}. This performs
     * no classloader isolation and while multiple {@link ElementDefinition} instances may be on the current classpath,
     * only one may exist on the supplied {@link Package} passed to this method.
     * </p>
     *
     * <p>
     * Results in a {@link ElementType#SHARED_CLASSPATH} {@link Element}
     * </p>
     *
     * @param elementRecord  the {@link Package} to the {@link Element}
     * @param serviceLocator a pre-configured {@link ServiceLocator}, from an existing source
     * @return the {@link Element}, loaded
     */
    ElementLoader getSharedLoader(
            ElementRecord elementRecord,
            ServiceLocator serviceLocator);

    /**
     * Finds the {@link Element} name, this will find the {@link ElementRecord} associated with it, throwing an
     * exception  if it is unable to find the {@link ElementDefinition} annotation. Used in constructing shared
     * elements.
     *
     * @param attributes  the attributes to use
     * @param selector    a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementRecord}
     */
    default Optional<ElementDefinitionRecord> findElementDefinitionRecord(
            Attributes attributes,
            Predicate<ElementDefinitionRecord> selector) {
        return findElementDefinitionRecord(
                getClass().getClassLoader(),
                attributes,
                selector
        );
    }

    /**
     * Finds the {@link Element} name, this will find the {@link ElementRecord} associated with it, return  an empty
     * optional if it is unable to find the {@link ElementDefinition} annotation. Used in constructing shared
     * elements.
     *
     * @param classLoader the classloader to scan
     * @param attributes  the attributes to use
     * @param selector    a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementRecord}
     */
    Optional<ElementDefinitionRecord> findElementDefinitionRecord(
            ClassLoader classLoader,
            Attributes attributes,
            Predicate<ElementDefinitionRecord> selector);

    /**
     * Given the {@link Package}, this will find the {@link ElementRecord} associated with it, throwing an exception
     * if it is unable to find the {@link ElementDefinition} annotation. Used in constructing shared elements.
     *
     * @param attributes the attributes to use
     * @param aPackage   the package to scan
     * @return the {@link ElementRecord}
     */
    ElementRecord getElementRecordFromPackage(Attributes attributes, Package aPackage);

    /**
     * Scans the supplied {@link Package} for specified
     *
     * @param aPackage a package to scan
     * @return a {@link Stream} of all {@link ElementServiceRecord}s exposed by the {@link Element}
     */
    Stream<ElementServiceRecord> getExposedServices(Package aPackage);

    /**
     * Gets a default {@link ElementLoaderFactory} via the {@link ServiceLoader} interface.
     *
     * @return the {@link ElementLoaderFactory}
     */
    static ElementLoaderFactory getDefault() {
        final var loader = ServiceLoader.load(ElementLoaderFactory.class);
        return loader.findFirst().orElseThrow(() -> new SdkException(
                "No SPI (Service Provider Implementation) for " +
                ElementLoaderFactory.class.getName())
        );
    }
    
    /**
     * <p>
     * A constructor type for a {@link ClassLoader}. Extending {@link Function} for clarity in documentation and
     * rationale, but otherwise just semantically identical to {@link Function}. However, as the name implies, it must
     * return a new instance for each invocation of {@link Function#apply}.
     * </p>
     */
    @FunctionalInterface
    interface ClassLoaderConstructor extends Function<ClassLoader, ClassLoader> {};

}
