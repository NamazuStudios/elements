package dev.getelements.elements.sdk.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.spi.ProvisionListener;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;

/**
 * Specifies a singleton {@link ElementRegistry} instance scoped with the name specified in {@link ElementRegistry#ROOT}
 * and makes it available to the entire application. In addition to the core {@link ElementRegistry} this allows for the
 * specification of additional {@link Element} instances.
 */
public class RootElementRegistryModule extends PrivateModule {

    private List<Supplier<ElementLoader>> loaders = new ArrayList<>();

    @Override
    protected void configure() {

        bind(Attributes.class)
                .to(GuiceAttributes.class)
                .asEagerSingleton();

        bind(ElementRegistry.class)
                .annotatedWith(named(ElementRegistry.ROOT))
                .toProvider(ElementRegistry::newDefaultInstance)
                .asEagerSingleton();

        final Key<ElementRegistry> rootElementRegistryKey = Key.get(
                ElementRegistry.class,
                named(ElementRegistry.ROOT)
        );

        expose(rootElementRegistryKey);

        final var key = Key.get(ElementRegistry.class);

        bindListener(
                binding -> key.equals(binding.getKey()),
                new ProvisionListener() {
                    @Override
                    public <T> void onProvision(final ProvisionInvocation<T> provision) {
                        final var root = (ElementRegistry) provision.provision();
                        loaders.stream().map(Supplier::get).forEach(root::register);
                    }

                });

        expose(Attributes.class);
        expose(rootElementRegistryKey);

    }

    public RootElementRegistryModule with(final ElementLoader loader) {
        return with(() -> loader);
    }

    public RootElementRegistryModule with(final Supplier<ElementLoader> loaderSupplier) {
        this.loaders.add(loaderSupplier);
        return this;
    }

}
