package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A filtering classloader that controls which classes from a delegate classloader can be loaded by checking
 * them against configured visibility rules. This classloader acts as a gatekeeper, allowing selective access
 * to types from the delegate classloader based on a combination of programmatic predicates and annotation-based
 * visibility declarations.
 * </p>
 *
 * <h2>Visibility Rules</h2>
 * <p>
 * When a class is requested, it is loaded from the delegate classloader and then evaluated against the following
 * rules in order. If any rule permits the class, it is returned; otherwise, a {@link ClassNotFoundException} is thrown:
 * </p>
 *
 * <ol>
 * <li><b>Permitted Types:</b> If any {@link PermittedTypes} predicate returns true for the class, it is permitted.
 * PermittedTypes implementations are discovered via {@link ServiceLoader} from the delegate classloader.</li>
 *
 * <li><b>Permitted Packages:</b> If any {@link PermittedPackages} predicate returns true for the class's package,
 * the class is permitted. PermittedPackages implementations are discovered via {@link ServiceLoader} from the
 * delegate classloader.</li>
 *
 * <li><b>@ElementPrivate Classes:</b> If the class is annotated with {@link ElementPrivate}, access is denied
 * (ClassNotFoundException is thrown). This takes precedence over @ElementPublic.</li>
 *
 * <li><b>@ElementPrivate Packages:</b> If the class's package is annotated with {@link ElementPrivate}, access
 * is denied for all classes in that package.</li>
 *
 * <li><b>@ElementPublic Classes:</b> If the class is annotated with {@link ElementPublic}, it is permitted.</li>
 *
 * <li><b>@ElementPublic Packages:</b> If the class's package is annotated with {@link ElementPublic}, all classes
 * in that package are permitted.</li>
 *
 * <li><b>Default Deny:</b> If none of the above rules permit the class, access is denied.</li>
 * </ol>
 *
 * <h2>Use Case</h2>
 * <p>
 * This classloader is designed for use in plugin architectures where you want to expose only specific types
 * from a base classloader to isolated plugins or Elements. By combining ServiceLoader-discovered predicates
 * with annotation-based visibility controls, you can create flexible and declarative type visibility policies.
 * </p>
 *
 * @see PermittedTypes
 * @see PermittedPackages
 * @see ElementPublic
 * @see ElementPrivate
 */
public class PermittedTypesClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(PermittedTypesClassLoader.class);

    static {
        registerAsParallelCapable();
    }

    private final ClassLoader delegate;

    private final List<Predicate<Class<?>>> permittedTypes;

    private final List<Predicate<Package>> permittedPackages;

    /**
     * The default constructor for the {@link PermittedTypesClassLoader}. This uses the result of
     * {@link Thread#getContextClassLoader()}. Failing that, it defaults to {@link #getSystemClassLoader()}.
     */
    public PermittedTypesClassLoader() {
        this(Thread.currentThread().getContextClassLoader() == null
                ? ClassLoader.getSystemClassLoader()
                : Thread.currentThread().getContextClassLoader()
        );
    }

    /**
     * Creates a new {@link PermittedTypesClassLoader} delegating to the supplied {@link ClassLoader}.
     *
     * @param delegate the delegate
     */
    public PermittedTypesClassLoader(final ClassLoader delegate) {
        this(delegate, null, null);
    }

    /**
     * Creates a new {@link PermittedTypesClassLoader} delegating to the supplied {@link ClassLoader} and parent.
     * @param delegate the delegate
     * @param parent the parent
     */
    public PermittedTypesClassLoader(final ClassLoader delegate,
                                     final ClassLoader parent) {
        this(delegate, null, parent);
    }

    /**
     * Creates a new {@link PermittedTypesClassLoader} delegatin to the supplied {@link ClassLoader} and parent.
     * This infers or specifies the name of the classloader.
     *
     * @param delegate the delegate
     * @param name the name, may be null
     * @param parent the parent
     */
    public PermittedTypesClassLoader(final ClassLoader delegate,
                                     final String name,
                                     final ClassLoader parent) {

        super(
            name == null
                ? "%s -> %s".formatted(PermittedTypesClassLoader.class.getSimpleName(), delegate.getName())
                : name,
            parent
        );

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

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            try {
                return super.loadClass(name, resolve);
            } catch (final ClassNotFoundException e) {
                final var delegateClass = delegate.loadClass(name);
                return processVisibilityAnnotations(delegateClass);
            } catch (NoClassDefFoundError e) {
                logger.trace("{} cannot load class {}", getName(), name, e);
                throw e;
            }
        }
    }

    private Class<?> processVisibilityAnnotations(final Class<?> aClass) throws ClassNotFoundException {

        final var aClassPackage = aClass.getPackage();

        if (permittedTypes.stream().anyMatch(t -> t.test(aClass))) {
            return aClass;
        } else if (permittedPackages.stream().anyMatch(p -> p.test(aClassPackage))) {
            return aClass;
        } else if (aClass.isAnnotationPresent(ElementPrivate.class)) {
            throw new ClassNotFoundException("%s is @%s".formatted(
                    aClass.getSimpleName(),
                    ElementPrivate.class.getSimpleName()
            ));
        } else if (aClassPackage.isAnnotationPresent(ElementPrivate.class)) {
            throw new ClassNotFoundException("%s's package (%s) is @%s".formatted(
                    aClass.getSimpleName(),
                    aClassPackage.getName(),
                    ElementPrivate.class.getSimpleName()
            ));
        } else if (aClass.isAnnotationPresent(ElementPublic.class)) {
            return aClass;
        } else if (aClassPackage.isAnnotationPresent(ElementPublic.class)) {
            return aClass;
        } else if (logger.isTraceEnabled()) {
            logger.trace(
                    "{} or {}'s package ({}) must have @{} annotation, be exposed via @{}, or one of [{}] or [{}]",
                    aClass.getSimpleName(),
                    aClass.getSimpleName(),
                    aClassPackage,
                    ElementPublic.class.getSimpleName(),
                    ElementDefinition.class.getSimpleName(),
                    permittedTypes
                            .stream()
                            .map(Object::getClass)
                            .map(Objects::toString)
                            .collect(Collectors.joining(",")),
                    permittedPackages
                            .stream()
                            .map(Object::getClass)
                            .map(Objects::toString)
                            .collect(Collectors.joining(","))
            );
        }

        throw new ClassNotFoundException(aClass.getName());

    }

}
