package dev.getelements.elements.sdk.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.spi.ProvisionListener;
import dev.getelements.elements.sdk.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

/**
 * Specifies a singleton {@link ElementRegistry} instance scoped with the name specified in {@link ElementRegistry#ROOT}
 * and makes it available to the entire application. In addition to the core {@link ElementRegistry} this allows for the
 * specification of additional {@link Element} instances.
 */
public class RootElementRegistryModule extends PrivateModule {

    private List<Supplier<ElementLoader>> loaders = new ArrayList<>();

    @Override
    protected void configure() {

        final Key<ElementRegistry> rootElementRegistryKey = Key.get(
                ElementRegistry.class,
                named(ROOT)
        );

        final Key<MutableElementRegistry> rootMutableElementRegistryKey = Key.get(
                MutableElementRegistry.class,
                named(ROOT)
        );

        expose(Attributes.class);
        expose(rootElementRegistryKey);
        expose(rootMutableElementRegistryKey);

        bind(Attributes.class)
                .to(GuiceAttributes.class)
                .asEagerSingleton();

        bind(rootElementRegistryKey)
                .to(rootMutableElementRegistryKey);

        bind(rootMutableElementRegistryKey)
                .toProvider(MutableElementRegistry::newDefaultInstance)
                .asEagerSingleton();

        bindListener(
                binding -> binding.getKey().equals(rootMutableElementRegistryKey),
                new ProvisionListener() {
                    @Override
                    public <T> void onProvision(final ProvisionInvocation<T> provision) {
                        final var root = (MutableElementRegistry) provision.provision();
                        loaders.stream().map(Supplier::get).forEach(root::register);
                    }

                });

    }

    public RootElementRegistryModule with(final ElementLoader loader) {
        return with(() -> loader);
    }

    public RootElementRegistryModule with(final Supplier<ElementLoader> loaderSupplier) {
        this.loaders.add(loaderSupplier);
        return this;
    }

}
