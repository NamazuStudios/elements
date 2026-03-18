package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.PermittedPackages;
import dev.getelements.elements.sdk.PermittedTypes;
import dev.getelements.elements.sdk.annotation.ElementLocal;
import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static dev.getelements.elements.sdk.spi.UrlUtils.toUrl;
import static java.util.Objects.requireNonNull;

/**
 * <p>
 * The primary classloader implementation used for isolated Element loading. This classloader creates a separate
 * class hierarchy for each Element while selectively allowing access to classes from a base (delegate) classloader.
 * It implements Element isolation through a combination of parent delegation, base classloader borrowing, and
 * visibility annotation processing.
 * </p>
 *
 * <h2>Classloader Architecture</h2>
 * <p>
 * {@code ElementClassLoader} operates with two key classloaders:
 * </p>
 * <ul>
 * <li><b>Parent Classloader:</b> Forms the standard delegation parent. Classes are first attempted to be loaded
 * from the parent using standard parent-first delegation. This typically contains the Element's SPI and API jars.</li>
 *
 * <li><b>Delegate Classloader:</b> Acts as the base classloader for selective type borrowing. When a class is not
 * found in the parent hierarchy, it's loaded from the delegate and checked against visibility rules. The delegate
 * typically represents the system classpath or a shared base classloader containing common libraries.</li>
 * </ul>
 *
 * <h2>Class Loading Process</h2>
 * <p>
 * When loading a class, the following steps are executed:
 * </p>
 * <ol>
 * <li><b>Parent Delegation:</b> First attempts to load the class through the parent classloader hierarchy using
 * standard Java delegation.</li>
 *
 * <li><b>@ElementLocal Handling (Parent):</b> If the class is found in the parent and is annotated with
 * {@link ElementLocal}, a unique copy of the class is created for this Element by reading the bytecode from
 * the delegate and defining it in this classloader. This ensures Element-local static state.</li>
 *
 * <li><b>Delegate Loading:</b> If the class is not found in the parent, attempts to load it from the delegate
 * classloader.</li>
 *
 * <li><b>@ElementLocal Handling (Delegate):</b> If the class from the delegate is annotated with
 * {@link ElementLocal}, creates a unique copy as described above.</li>
 *
 * <li><b>Initialization Check:</b> Ensures the {@link ElementRecord} has been set. If not, the Element is still
 * initializing and the class is denied (helps prevent improper Element configuration).</li>
 *
 * <li><b>Registered Service Check:</b> If the class is a registered service (exposed via the Element's service
 * exports), it is implicitly permitted and returned.</li>
 *
 * <li><b>TypeRequest Check:</b> If any {@link dev.getelements.elements.sdk.record.ElementTypeRequestRecord} in the
 * {@link ElementRecord} permits the binary class name, the class is returned.</li>
 *
 * <li><b>PackageRequest Check:</b> If any {@link dev.getelements.elements.sdk.record.ElementPackageRequestRecord}
 * in the {@link ElementRecord} permits the class's package name, the class is returned.</li>
 *
 * <li><b>Default Deny:</b> Otherwise, the class is denied with {@link ClassNotFoundException}.</li>
 * </ol>
 *
 * <h2>Special Features</h2>
 *
 * <h2>@ElementLocal Support</h2>
 * <p>
 * Classes annotated with {@link ElementLocal} are bytecode-copied into this classloader, creating Element-specific
 * instances. This allows each Element to have its own version of utility classes with separate static state,
 * preventing interference between Elements.
 * </p>
 *
 * <h2>Built-in Resource Overrides</h2>
 * <p>
 * Provides Element-specific implementations of certain service provider interfaces by overriding resource loading
 * for {@code META-INF/services} files. This allows each Element to have its own scoped services like
 * {@code ElementSupplier} and {@code ElementRegistrySupplier}.
 * </p>
 *
 * <h2>Service Export Visibility</h2>
 * <p>
 * Classes registered as exposed services in the Element's {@link ElementRecord} are automatically made public,
 * allowing them to be accessed by other Elements or the host application without requiring visibility annotations.
 * </p>
 *
 * <h2>Differences from PermittedTypesClassLoader</h2>
 * <p>
 * While {@link dev.getelements.elements.sdk.PermittedTypesClassLoader} uses {@link PermittedTypes} and
 * {@link PermittedPackages} predicates for visibility control, {@code ElementClassLoader} uses Element-specific
 * visibility rules based on service registration and initialization state. {@code ElementClassLoader} is designed
 * specifically for Element isolation, while {@code PermittedTypesClassLoader} is a more general-purpose filtering
 * classloader.
 * </p>
 *
 * @see ElementLocal
 * @see ElementPublic
 * @see ElementPrivate
 * @see ElementRecord
 * @see dev.getelements.elements.sdk.PermittedTypesClassLoader
 */
public class ElementImplementationClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ElementImplementationClassLoader.class);

    private static final ClassLoaderUtils utils = new ClassLoaderUtils(ElementImplementationClassLoader.class);

    private static final Map<String, String> BUILTIN_RESOURCES = Map.of(
            "META-INF/services/dev.getelements.elements.sdk.ElementSupplier",
            "text://dev.getelements.elements.sdk.spi.ElementScopedElementSupplier",
            "META-INF/services/dev.getelements.elements.sdk.ElementRegistrySupplier",
            "text://dev.getelements.elements.sdk.spi.ElementScopedElementRegistrySupplier"
    );

    private ElementRecord elementRecord;

    private final ClassLoader delegate;

    static {
        registerAsParallelCapable();
    }

    /**
     * Creates a new instance of {@link ElementImplementationClassLoader} with the specified delegate class loader. The delegate
     * loader will be the sources for classes that are not found in this class loader. This is typically the system
     * class path and the Element's class path.
     *
     * This constructor uses the bootstrap as the parent class loader.
     *
     * @param delegate the delegate class loader to use for loading classes that are not found in this class loader.
     */
    public ElementImplementationClassLoader(final ClassLoader delegate) {
        this(delegate, null);
    }

    /**
     * Creates a new instance of {@link ElementImplementationClassLoader} with the specified delegate class loader. The delegate
     * loader will be the sources for classes that are not found in this class loader. This is typically the system
     * class path and the Element's class path.
     *
     * @param delegate the delegate class loader to use for loading classes that are not found in this class loader.
     * @param parent the parent class loader to use for loading classes that are not found in this class loader.
     */
    public ElementImplementationClassLoader(final ClassLoader delegate, final ClassLoader parent) {
        super("Element Class Loader", parent);
        this.delegate = requireNonNull(delegate, "delegate");
    }

    public ElementRecord getElementRecord() {
        return elementRecord;
    }

    public void setElementRecord(final ElementRecord elementRecord) {
        this.elementRecord = elementRecord;
    }

    @Override
    protected URL findResource(final String name) {
        final var url = BUILTIN_RESOURCES.getOrDefault(name, null);
        return url == null ? null : toUrl(url);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {

        final var url = BUILTIN_RESOURCES.getOrDefault(name, null);
        final var all = new ArrayList<URL>();

        if (url != null) {
            all.add(toUrl(url));
        } else if (!name.startsWith("META-INF/services/")) {
            final var delegateUrls = delegate.getResources(name);
            if (delegateUrls != null) delegateUrls.asIterator().forEachRemaining(all::add);
        }

        return Collections.enumeration(all);

    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

        synchronized (getClassLoadingLock(name)) {


            Class<?> fromSuper;
            Class<?> fromDelegate;

            try {
                fromDelegate = delegate.loadClass(name);
            } catch (ClassNotFoundException ex) {
                fromDelegate = null;
            }

            try {
                fromSuper = super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                fromSuper = null;
            }

            if (fromDelegate == null) {
                // The delegate does not provide this type at all. Therefore, it's a no-brainer. We jsut return what we
                // laoded. There is no conflict because the delegate does not define the class. If this call fails, then
                // it's because we truly can't find the class.
                return super.loadClass(name, resolve);
            }

            if (isLocal(fromDelegate)) {

                final var alreadyLoaded = findLoadedClass(name);

                if (alreadyLoaded != null) {
                    // If the class was already loaded, we try to find it. If it's found then the checks below have
                    // already been processed. This ensures there is no double-loading problem which almost guarantees
                    // a crash on subsequent loads.
                    return alreadyLoaded;
                } else {
                    // We use the delegate provided instance of the class. The class
                    return copyFromDelegate(fromDelegate);
                }

            } else if (fromSuper != null) {
                // If there is a class loaded from the superclass, then we just load it. This should be considered a
                // normal superclass
                return fromSuper;
            } else {
                // We shouldn't pull from the delegate. We have already tried to load the class. We should give up
                // because it's not in this classloader either.
                throw new ClassNotFoundException(name);
            }

        }
    }

    private boolean isLocal(final Class<?> aClass) {
        return aClass.getAnnotation(ElementLocal.class) != null;
    }

    private Class<?> copyFromDelegate(final Class<?> aClass) {

        final var clsName = aClass.getName();
        final var registryResourceURL = clsName.replace(".", "/") + ".class";

        try (var is = delegate.getResourceAsStream(registryResourceURL);
             var os = new ByteArrayOutputStream()) {

            assert is != null;
            is.transferTo(os);

            final var bytes = os.toByteArray();
            return defineClass(clsName, bytes, 0, bytes.length);

        } catch (IOException ex) {
            throw new SdkException(ex);
        }

    }

}
