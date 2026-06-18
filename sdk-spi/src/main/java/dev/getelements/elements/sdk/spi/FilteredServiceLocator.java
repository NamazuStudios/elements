package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.exception.SdkServiceNotFoundException;
import dev.getelements.elements.sdk.record.ElementServiceKey;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class FilteredServiceLocator implements ServiceLocator {

    private final ServiceLocator delegate;

    private final Set<ElementServiceKey<?>> serviceKeySet;

    public FilteredServiceLocator(final ServiceLocator delegate,
                                  final Set<ElementServiceKey<?>> serviceKeySet) {
        this.delegate = delegate;
        this.serviceKeySet = Set.copyOf(serviceKeySet);
    }

    @Override
    public <T> Optional<Supplier<T>> findInstance(final ElementServiceKey<T> key) {

        if (serviceKeySet.stream().anyMatch(k -> k.type() == key.type())) {
            return delegate.findInstance(key);
        }

        throw new SdkServiceNotFoundException("Service not found: " + key);

    }

}
