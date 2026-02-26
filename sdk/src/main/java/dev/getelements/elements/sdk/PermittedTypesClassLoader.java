package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.exception.SdkDuplicateClassError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * Indicates the {@link DuplicateTypeStrategy} to use when loading types.
     */
    public static final DuplicateTypeStrategy DUPLICATE_TYPE_STRATEGY;

    /**
     *
     */
    public static final String DUPLICATE_TYPE_STRATEGY_KEY = "dev.getelements.elements.sdk.duplicate.type.strategy";

    static {

        registerAsParallelCapable();

        final var duplicateTypeStrategy = System.getProperty(
                DUPLICATE_TYPE_STRATEGY_KEY,
                DuplicateTypeStrategy.LINKAGE_ERROR.name()
        );

        DUPLICATE_TYPE_STRATEGY = Stream.of(DuplicateTypeStrategy.values())
                .filter(s -> duplicateTypeStrategy.equals(s.name()))
                .findFirst()
                .orElse(DuplicateTypeStrategy.LINKAGE_ERROR);

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

            Class<?> fromDelegate;

            try {
                fromDelegate = delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
                fromDelegate = null;
                logger.trace("Class `{}` not found in delegate.", name);
            }

            try {

                final var fromParent = super.loadClass(name, resolve);

                if (fromDelegate != null && isVisible(fromDelegate) && fromParent != fromDelegate) {
                    return DUPLICATE_TYPE_STRATEGY.handle(fromParent, fromDelegate);
                } else {
                    return fromParent;
                }

            } catch (final ClassNotFoundException e) {
                if (fromDelegate == null) {
                    throw new ClassNotFoundException(name);
                } else {
                    return processVisibilityAnnotations(fromDelegate);
                }
            }
        }
    }

    private boolean isVisible(final Class<?> aClass) {

        final var aClassPackage = aClass.getPackage();
        final var aClassPackageInfo = ElementReflectionUtils.getInstance().findPackageInfo(aClass);

        if (permittedTypes.stream().anyMatch(t -> t.test(aClass)))
            return true;
        else if (permittedPackages.stream().anyMatch(p -> p.test(aClassPackage)))
            return true;
        else if (aClass.isAnnotationPresent(ElementPrivate.class))
            return false;
        else if (aClassPackageInfo.map(c -> c.isAnnotationPresent(ElementPrivate.class)).orElse(false))
            return false;
        else if (aClass.isAnnotationPresent(ElementPublic.class))
            return true;
        else if (aClassPackageInfo.map(c -> c.isAnnotationPresent(ElementPublic.class)).orElse(false))
            return true;
        else
            return false;

    }

    private Class<?> processVisibilityAnnotations(final Class<?> aClass) throws ClassNotFoundException {

        if (isVisible(aClass)) {
            return aClass;
        }

        logger.trace(
                "{} or {}'s package ({}) must have @{} annotation, be exposed via @{}, or one of [{}] or [{}]",
                aClass.getSimpleName(),
                aClass.getSimpleName(),
                aClass.getPackage(),
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

        throw new ClassNotFoundException(aClass.getName());

    }

    /**
     * Indicates how to handle a duplicate type situation. This occurs when the delegate and the Element both expose
     * the same type. This is usually an error, but situations may arise where we want to force this to happen one way
     * or another. Therefore, we provide the following strategies to handle the situation. The behavior of the
     * {@link PermittedTypesClassLoader} is governed by the system define {@link #DUPLICATE_TYPE_STRATEGY_KEY}.
     */
    public enum DuplicateTypeStrategy {

        /**
         * Logs a warning and uses the parent class.
         */
        PARENT((fromParent, fromDelegate) -> {

            logger.warn("Duplicate class definition for `{}`: found in `{}` and `{}`. Using type from parent.",
                    fromParent.getName(),
                    fromParent.getClassLoader() == null
                            ? "bootstrap"
                            : fromParent.getClassLoader().getName(),
                    fromDelegate.getClassLoader() == null
                            ? "bootstrap"
                            : fromDelegate.getClassLoader().getName()
            );

            return fromParent;

        }),

        /**
         * Logs a warning and uses the delegate class.
         */
        DELEGATE((fromParent, fromDelegate) -> {

            logger.warn("Duplicate class definition for `{}`: found in `{}` and `{}`. Using type from delegate.",
                    fromParent.getName(),
                    fromParent.getClassLoader() == null
                            ? "bootstrap"
                            : fromParent.getClassLoader().getName(),
                    fromDelegate.getClassLoader() == null
                            ? "bootstrap"
                            : fromDelegate.getClassLoader().getName()
            );

            return fromDelegate;

        }),

        /**
         * Throws a linkage error.
         */
        LINKAGE_ERROR((fromParent, fromDelegate) -> {
            throw new SdkDuplicateClassError(
                    "Duplicate class definition for `%s`: found in `%s` and `%s`.".formatted(
                            fromParent.getName(),
                            fromParent.getClassLoader() == null
                                    ? "bootstrap"
                                    : fromParent.getClassLoader().getName(),
                            fromDelegate.getClassLoader() == null
                                    ? "bootstrap"
                                    : fromDelegate.getClassLoader().getName()
                    )
            );
        });

        private final BiFunction<Class<?>, Class<?>, Class<?>> handler;

        DuplicateTypeStrategy(final BiFunction<Class<?>, Class<?>, Class<?>> handler) {
            this.handler = handler;
        }

        /**
         * Handles the discrepancy according to the strategy.
         *
         * @param fromParent the class from the parent
         * @param fromDelegate the class from the delegate
         * @return the class selected
         */
        private Class<?> handle(final Class<?> fromParent, final Class<?> fromDelegate) {
            return handler.apply(fromParent, fromDelegate);
        }

    }

}
