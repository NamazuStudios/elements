package dev.getelements.elements.sdk.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.record.ElementServiceKey;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;

/**
 * Created by patricktwohig on 9/1/15.
 */
public class GuiceServiceLocator implements ServiceLocator {

    private Injector injector;

    @Override
    public <T> Optional<Supplier<T>> findInstance(final ElementServiceKey<T> elementServiceKey) {

        final var guiceKey = elementServiceKey.isNamed()
            ? Key.get(elementServiceKey.type(), named(elementServiceKey.name()))
            : Key.get(elementServiceKey.type());

        final var binding = getInjector().getBinding(guiceKey);
        return Optional.ofNullable(binding).map(b -> b.getProvider()::get);

    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
