package dev.getelements.elements.sdk.guice;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.util.ImmutableAttributes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.google.inject.name.Names.named;

public class GuiceAttributes implements Attributes {

    private Injector injector;

    private final AtomicReference<Set<String>> names = new AtomicReference<>();

    @Override
    public Set<String> getAttributeNames() {

        var result = names.get();

        while (result == null) {

            final var toSet = doGetAttributeNames();

            if (names.compareAndSet(null, toSet)) {
                result = toSet;
                break;
            }

        }

        return result;

    }

    private Set<String> doGetAttributeNames() {
        return getInjector()
                .getAllBindings()
                .values()
                .stream()
                .map(Binding::getKey)
                .filter(k -> String.class.equals(k.getTypeLiteral().getRawType()))
                .filter(k -> Named.class.isAssignableFrom(k.getAnnotationType()))
                .map(k -> (Named) k.getAnnotation())
                .map(Named::value)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Object> getAttributeOptional(final String name) {
        final var key = Key.get(String.class, named(name));
        final var binding = getInjector().getAllBindings().get(key);
        return Optional.ofNullable(binding).map(b -> binding.getProvider().get());
    }

    @Override
    public Attributes immutableCopy() {
        return ImmutableAttributes.copyOf(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Attributes.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return Attributes.hashCode(this);
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
