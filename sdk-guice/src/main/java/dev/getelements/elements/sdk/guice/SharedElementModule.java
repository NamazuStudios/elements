package dev.getelements.elements.sdk.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.spi.ProvisionListener;
import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.record.ElementServiceRecord;
import dev.getelements.elements.sdk.util.reflection.ElementReflectionUtils;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;

/**
 * Defines a new {@link Element} which is sourced from the guice bindings visible to the module at the time the injector
 * was created. This also assumes there exists an {@link ElementRegistry} named {@link ElementRegistry#ROOT} which will
 * be the parent of the newly created {@link ElementRegistry}. In addition to the {@link ElementLoader} specified in the
 * constructor of this module, it is possible to specify additional {@link Element} instances to load within the same
 * {@link ElementLoader}.
 */
public class SharedElementModule extends PrivateModule {

    private final ElementLoaderFunction loader;

    private final ElementExposureFunction exposure;

    public SharedElementModule(final String packageName) {
        this(ElementReflectionUtils.getInstance().getPackageForElementsAnnotations(packageName));
    }

    public SharedElementModule(final Package aPackage) {

        final var loaderFactory = ElementLoaderFactory.getDefault();

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

        loader = (attributesProvider, serviceLocatorProvider) -> {
            final var attributes = attributesProvider.get();
            final var serviceLocator = serviceLocatorProvider.get();
            final var elementRecord = loaderFactory.getElementRecord(attributes, aPackage);
            return loaderFactory.getSharedLoader(elementRecord, serviceLocator);
        };

        exposure = () -> exposedServiceKeys.forEach(this::expose);

    }

    @Override
    protected final void configure() {

        final var rootElementRegistryKey = Key.get(
                MutableElementRegistry.class,
                named(ElementRegistry.ROOT)
        );

        requireBinding(rootElementRegistryKey);

        final var attributesProvider = getProvider(Attributes.class);
        final var serviceLocatorProvider = getProvider(ServiceLocator.class);
        final var rootElementRegistryProvider = getProvider(rootElementRegistryKey);

        final var injectorProvider = getProvider(Injector.class);

        bind(ServiceLocator.class).toProvider(() -> {
            final var injector = injectorProvider.get();
            final var serviceLocator = new GuiceServiceLocator();
            serviceLocator.setInjector(injector);
            return serviceLocator;
        });

        bind(ElementRegistry.class).toProvider(() -> {
            final var root = rootElementRegistryProvider.get();
            final var loader = this.loader.apply(attributesProvider, serviceLocatorProvider);
            final var element = root.register(loader);
            return element.getElementRegistry();
        }).asEagerSingleton();

        exposure.run();
        configureElement();

    }

    /**
     * Configures the {@link Element} which will be used in this {@link SharedElementModule}.
     */
    protected void configureElement() {}

    @FunctionalInterface
    private interface ElementLoaderFunction extends BiFunction<
            Provider<Attributes>,
            Provider<ServiceLocator>,
            ElementLoader> {}

    @FunctionalInterface
    private interface ElementExposureFunction extends Runnable {}

}
