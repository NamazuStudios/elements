package dev.getelements.elements.sdk.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Scope;
import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.record.ElementServiceRecord;
import dev.getelements.elements.sdk.util.reflection.ElementReflectionUtils;

import static com.google.inject.name.Names.named;

/**
 * Defines a new {@link Element} which is sourced from the guice bindings visible to the module at the time the injector
 * was created. This also assumes there exists an {@link ElementRegistry} named {@link ElementRegistry#ROOT} which will
 * be the parent of the newly created {@link ElementRegistry}. In addition to the {@link ElementLoader} specified in the
 * constructor of this module, it is possible to specify additional {@link Element} instances to load within the same
 * {@link ElementLoader}.
 */
public class SharedElementModule extends PrivateModule {

    protected final Package aPackage;

    public SharedElementModule(final String packageName) {
        this(ElementReflectionUtils.getInstance().getPackageForElementsAnnotations(packageName));
    }

    public SharedElementModule(final Package aPackage) {
        this.aPackage = aPackage;
    }

    @Override
    protected final void configure() {

        final var loaderFactory = ElementLoaderFactory.getDefault();

        final var rootElementRegistryKey = Key.get(
                MutableElementRegistry.class,
                named(ElementRegistry.ROOT)
        );

        final var exposedServiceKeys = loaderFactory
                .getExposedServices(aPackage)
                .map(ElementServiceRecord::export)
                .flatMap(export -> export
                        .exposed()
                        .stream()
                        .map(type -> export.isNamed()
                                ? Key.get(type, named(export.name()))
                                : Key.get(type))
                )
                .toList();

        requireBinding(rootElementRegistryKey);

        final var injectorProvider = getProvider(Injector.class);
        final var attributesProvider = getProvider(Attributes.class);
        final var serviceLocatorProvider = getProvider(ServiceLocator.class);

        final var elementProvider = getProvider(Element.class);
        final var rootElementRegistryProvider = getProvider(rootElementRegistryKey);

        bind(ServiceLocator.class).toProvider(() -> {
            final var injector = injectorProvider.get();
            final var serviceLocator = new GuiceServiceLocator();
            serviceLocator.setInjector(injector);
            return serviceLocator;
        });

        bind(Element.class).toProvider(() -> {
            final var root = rootElementRegistryProvider.get();
            final var attributes = attributesProvider.get();
            final var serviceLocator = serviceLocatorProvider.get();
            final var elementRecord = loaderFactory.getElementRecordFromPackage(attributes, aPackage);
            final var loader = loaderFactory.getSharedLoader(elementRecord, serviceLocator);
            return root.register(loader);
        }).asEagerSingleton();

        bind(ElementRegistry.class).toProvider(() -> {
            final var element = elementProvider.get();
            return element.getElementRegistry();
        }).asEagerSingleton();

        exposedServiceKeys.forEach(this::expose);

        final var threadLocalAttributesScope = new ReentrantThreadLocalScope<>(
                ElementScope.class,
                () -> elementProvider.get().getCurrentScope(),
                ElementScope::getMutableAttributes
        );

        configureElement(threadLocalAttributesScope);

    }

    /**
     * Configures the {@link Element} which will be used in this {@link SharedElementModule}.
     */
    protected void configureElement() {}

    /**
     * Configures the {@link Element} which will be used in this {@link SharedElementModule}.
     *
     * @param thredLocalScope the thread local scope for the {@link Element}
     */
    protected void configureElement(final Scope thredLocalScope) {
        configureElement();
    }

}
