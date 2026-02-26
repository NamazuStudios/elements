package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceRecord;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;

/**
 * <p>
 * The {@link ElementLoader} factory. This factory creates loaders for Elements, which can be configured with either
 * shared or isolated classloading strategies.
 * </p>
 *
 * <h2>Element Types</h2>
 *
 * <h2>Shared Elements ({@link ElementType#SHARED_CLASSPATH})</h2>
 * <p>
 * A shared element is a wrapper around types from a single classloader, which is usually the system classloader.
 * Shared elements do not provide classloader isolation and share the same classpath as the host application.
 * Use {@link #getSharedLoader(ElementRecord, ServiceLocator)} to create shared element loaders.
 * </p>
 *
 * <h2>Isolated Elements ({@link ElementType#ISOLATED_CLASSPATH})</h2>
 * <p>
 * An isolated classloader allows the Element to have its own isolated implementation classpath, preventing conflicts
 * between Elements and the rest of the system. This is the recommended approach for production deployments.
 * Use the {@code getIsolatedLoader} methods to create isolated element loaders.
 * </p>
 *
 * <h2>Classloader Structure for Isolated Elements</h2>
 * <p>
 * The classloader structure for an isolated Element consists of three components:
 * </p>
 * <ul>
 * <li><b>Base Classloader:</b> The classloader from which the Element can selectively "borrow" types. This is
 * typically the system classloader or a custom classloader containing shared libraries. Types such as
 * {@code PermittedPackages} and {@code PermittedTypes} defined in the base classloader control which classes
 * can flow between the base classloader and the Element's implementation.</li>
 *
 * <li><b>Parent Classloader:</b> An optional classloader that may contain common APIs, SPIs, and utilities
 * useful for loading the Element. This sits between the base classloader and the implementation classloader
 * in the delegation hierarchy.</li>
 *
 * <li><b>Implementation Classloader:</b> The classloader that loads the actual Element implementation and its
 * dependencies. This is an isolating classloader that keeps the implementation clean from other Elements and
 * the rest of the system while selectively borrowing from the base classloader. The isolating classloader
 * also provides static instances (such as the Element itself) that are visible only to that specific Element.</li>
 * </ul>
 *
 * <p>
 * The result is a hierarchy starting at the parent classloader (if provided), with an isolating classloader
 * keeping the Element's implementation isolated from other Elements and the system, while selectively borrowing
 * permitted types from the base classloader.
 * </p>
 *
 * <h2>Practical Note on PermittedTypesClassLoader Placement</h2>
 * <p>
 * In virtually all scenarios, if {@link dev.getelements.elements.sdk.PermittedTypesClassLoader} is used for
 * selective type borrowing, it must be placed in the parent classloader hierarchy (not as the base classloader).
 * {@link dev.getelements.elements.sdk.PermittedTypesClassLoader} acts as a filtering layer that can wrap any
 * classloader to provide selective type access. While it typically wraps the same classloader used as the base
 * classloader, it doesn't have to. Placing it in the parent ensures proper delegation and type visibility control
 * during Element loading.
 * </p>
 *
 * <h2>VM Restrictions and Warnings</h2>
 * <p>
 * Some VM restrictions apply when using isolated classloaders. If an Element accesses the system classloader
 * directly (for example, via {@code ClassLoader.getSystemClassLoader()} instead of
 * {@code Thread.getContextClassLoader()}), it may result in strange or unpredictable behavior, including:
 * </p>
 * <ul>
 * <li>Loading classes from the wrong classloader, bypassing isolation</li>
 * <li>ClassCastExceptions when the same class is loaded by different classloaders</li>
 * <li>Unexpected visibility of types that should be isolated</li>
 * <li>Loss of static instance isolation</li>
 * <li>Strange VM errors related to missing types</li>
 * </ul>
 * <p>
 * Elements should use {@code Thread.getContextClassLoader()} to access their classloader context and avoid
 * direct system classloader access.
 * </p>
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
     * <p>
     * The returned {@link ElementLoader} will use the calling class's classloader as the base classloader for
     * selective type borrowing, with no explicit parent classloader (defaults to bootstrap).
     * </p>
     *
     * @param attributes the attributes to use
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoader(
            Attributes attributes,
            ClassLoaderConstructor classLoaderCtor) {
        return getIsolatedLoader(
                attributes,
                currentThread().getContextClassLoader(),
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
     * <p>
     * The returned {@link ElementLoader} will use the supplied base classloader for selective type borrowing,
     * with no explicit parent classloader (defaults to bootstrap).
     * </p>
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader} used for selective type borrowing
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
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
     * <p>
     * The returned {@link ElementLoader} will use the calling class's classloader as the base classloader for
     * selective type borrowing, with no explicit parent classloader (defaults to bootstrap).
     * </p>
     *
     * @param attributes the attributes to use
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
     * @param selector a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementLoader}
     */
    default ElementLoader getIsolatedLoader(
            Attributes attributes,
            ClassLoaderConstructor classLoaderCtor,
            Predicate<ElementDefinitionRecord> selector) {
        return getIsolatedLoader(
                attributes,
                currentThread().getContextClassLoader(),
                classLoaderCtor,
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
     * <p>
     * The returned {@link ElementLoader} will use the supplied base classloader for selective type borrowing,
     * with no explicit parent classloader (defaults to bootstrap).
     * </p>
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader} used for selective type borrowing
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
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
                (ClassLoader) null,
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
     * <p>
     * The returned {@link ElementLoader} will use the supplied base classloader for selective type borrowing.
     * The parent classloader is derived from the provided {@link Element}.
     * </p>
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader} used for selective type borrowing
     * @param element the parent {@link Element} to use for deriving the parent classloader, may be null indicating
     *        that no explicit parent classloader should be used (defaults to bootstrap)
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
     * @return the {@link ElementLoader}
     * @deprecated In 3.7 onward an Element shouldn't have a parent
     */
    default ElementLoader getIsolatedLoaderWithParent(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor,
            Element element) {

        if (element != null) {
            var logger = LoggerFactory.getLogger(getClass());
            logger.warn("Attempting to load an Element with a parent. This is deprecated functionality.");
        }

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
     * <p>
     * The returned {@link ElementLoader} will use the supplied base classloader for selective type borrowing.
     * The parent classloader is derived from the provided {@link Element}, or null if not specified (defaults to bootstrap).
     * </p>
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader} used for selective type borrowing
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
     * @param element the parent {@link Element} to use for deriving the parent classloader, may be null indicating
     *                that no explicit parent classloader should be used (defaults to bootstrap)
     * @param selector a {@link Predicate} to select a single {@link ElementDefinitionRecord} to load
     * @return the {@link ElementLoader}
     * @deprecated In 3.7 onward an Element shouldn't have a parent
     */
    default ElementLoader getIsolatedLoaderWithParent(
            Attributes attributes,
            ClassLoader baseClassLoader,
            ClassLoaderConstructor classLoaderCtor,
            Element element,
            Predicate<ElementDefinitionRecord> selector) {

        if (element != null) {
            var logger = LoggerFactory.getLogger(getClass());
            logger.warn("Attempting to load an Element with a parent. This is deprecated functionality.");
        }

        return getIsolatedLoaderWithParent(
                attributes,
                baseClassLoader,
                classLoaderCtor,
                element == null
                        ? null
                        : element.getElementRecord().classLoader(),
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
     * <p>
     * The returned {@link ElementLoader} will use the supplied base classloader for selective type borrowing,
     * with an explicit parent classloader forming the delegation parent of the implementation classloader.
     * </p>
     *
     * <p>
     * Note: The {@link Element} must have an {@link ElementLoader} SPI implementation defined on its classpath.
     * </p>
     *
     * @param attributes the attributes to use
     * @param baseClassLoader the base {@link ClassLoader} used for selective type borrowing
     * @param classLoaderCtor the {@link ClassLoader} constructor that creates the implementation classloader
     * @param parent the parent {@link ClassLoader} that forms the delegation parent of the implementation
     *               classloader, may be null indicating no explicit parent (defaults to bootstrap)
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
                currentThread().getContextClassLoader(),
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
    interface ClassLoaderConstructor extends Function<ClassLoader, ClassLoader> {}

}
