package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.PermittedPackages;
import dev.getelements.elements.sdk.PermittedTypes;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
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
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.spi.UrlUtils.toUrl;
import static dev.getelements.elements.sdk.spi.UrlUtils.toUrls;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link ClassLoader} type which inspects classes at load time processing the visibility annotations provided by the
 * Elements' SDK. Such annotations include {@link ElementPrivate} and {@link ElementPublic}. This implementation
 * allows for a separate hierarchy of classes to be loaded for each Element, while still allowing some parts of the
 * core system through. The delegate {@link ClassLoader} is typically the system class loader.
 */
public class ElementClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ElementClassLoader.class);

    private static final ClassLoaderUtils utils = new ClassLoaderUtils(ElementClassLoader.class);

    private static final Map<String, String> BUILTIN_RESOURCES = Map.of(
            "META-INF/services/dev.getelements.elements.sdk.ElementSupplier",
            "text://dev.getelements.elements.sdk.spi.ElementScopedElementSupplier",
            "META-INF/services/dev.getelements.elements.sdk.ElementRegistrySupplier",
            "text://dev.getelements.elements.sdk.spi.ElementScopedElementRegistrySupplier"
    );

    private ElementRecord elementRecord;

    private final ClassLoader delegate;

    private final List<Predicate<Class<?>>> permittedTypes;

    private final List<Predicate<Package>> permittedPackages;

    /**
     * Creates a new instance of {@link ElementClassLoader} with the specified delegate class loader. The delegate
     * loader will be the sources for classes that are not found in this class loader. This is typically the system
     * class path and the Element's class path.
     *
     * This constructor uses the bootstrap as the parent class loader.
     *
     * @param delegate the delegate class loader to use for loading classes that are not found in this class loader.
     */
    public ElementClassLoader(final ClassLoader delegate) {
        this(delegate, null);
    }

    /**
     * Creates a new instance of {@link ElementClassLoader} with the specified delegate class loader. The delegate
     * loader will be the sources for classes that are not found in this class loader. This is typically the system
     * class path and the Element's class path.
     *
     * @param delegate the delegate class loader to use for loading classes that are not found in this class loader.
     * @param parent the parent class loader to use for loading classes that are not found in this class loader.
     */
    public ElementClassLoader(final ClassLoader delegate, final ClassLoader parent) {
        super("Element Class Loader", parent);

        this.delegate = requireNonNull(delegate, "delegate");

        permittedTypes = ServiceLoader
                .load(PermittedTypes.class, delegate)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableList());

        permittedPackages = ServiceLoader
                .load(PermittedPackages.class, delegate)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableList());

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
        final var delegateUrls = delegate.getResources(name);

        final var all = new ArrayList<URL>();

        if (url != null) {
            all.add(toUrl(url));
        }

        if (delegateUrls != null) {
            delegateUrls.asIterator().forEachRemaining(all::add);
        }

        return Collections.enumeration(all);

    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        try {

            final var aClass = super.loadClass(name, resolve);

            if (aClass.getAnnotation(ElementLocal.class) != null) {
                final var aLocalClass = findLoadedClass(name);
                return aLocalClass == null ? copyFromDelegate(aClass) : aLocalClass;
            }

            return aClass;

        } catch (final ClassNotFoundException e) {
            final var delegateClass = delegate.loadClass(name);
            return processVisibilityAnnotations(delegateClass);
        }
    }

    private Class<?> processVisibilityAnnotations(final Class<?> aClass) throws ClassNotFoundException {

        // SPI Types annotated as ElementLocal will be copied from the bytecode of the parent and then loaded into this
        // classloader. This ensures that the Element Local types are unique per Element.

        if (aClass.getAnnotation(ElementLocal.class) != null) {
            final var name = aClass.getName();
            final var aLocalClass = findLoadedClass(name);
            return aLocalClass == null ? copyFromDelegate(aClass) : aLocalClass;
        }

        final var aClassPackage = aClass.getPackage();

        // If the class is part of the whitelist that's builtin, then we honor it. This may be something we want to
        // make configurable based on application attributes later, but we'll stitck to hardcoded values we have for
        // now. We will likely need to extend more.

        if (permittedTypes.stream().anyMatch(t -> t.test(aClass))) {
            return aClass;
        }

        if (permittedPackages.stream().anyMatch(p -> p.test(aClassPackage))) {
            return aClass;
        }

        if (aClass.isAnnotationPresent(ElementPrivate.class)) {
            throw new ClassNotFoundException(format(
                    "%s is @%s",
                    aClass.getSimpleName(),
                    ElementPrivate.class.getSimpleName()
            ));
        }

        if (aClassPackage.isAnnotationPresent(ElementPrivate.class)) {

            final var message = format("%s's package (%s) is @%s",
                    aClass.getSimpleName(),
                    aClassPackage.getName(),
                    ElementPrivate.class.getSimpleName()
            );

            throw new ClassNotFoundException(message);

        }

        if (aClass.isAnnotationPresent(ElementPublic.class)) {
            return aClass;
        }

        if (aClassPackage.isAnnotationPresent(ElementPublic.class)) {
            return aClass;
        }

        if (getElementRecord() == null) {
            // This ensures that if the ElementRecord is not set, we aren't done initializing the Element. Anything
            // past this point should exist in the Element's ClassLoader. If it doesn't, then the Element isn't
            // properly configured (eg it's missing an SPI).
            throw new ClassNotFoundException();
        }

        final var isRegisteredService = getElementRecord()
                .services()
                .stream()
                .flatMap(svc -> svc.export().exposed().stream())
                .anyMatch(exposed -> exposed.equals(aClass));

        if (isRegisteredService) {
            // This implicitly makes a registered service public.
            return aClass;
        }

        logger.error("{} or {}'s package ({}) must have @{} annotation or be exposed via @{}",
                aClass.getSimpleName(),
                aClass.getSimpleName(),
                aClassPackage,
                ElementPublic.class.getSimpleName(),
                ElementDefinition.class.getSimpleName()
        );

        throw new ClassNotFoundException(aClass.getName());

    }

    private Class<?> copyFromDelegate(final Class<?> parentClass) {

        final var clsName = parentClass.getName();
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
