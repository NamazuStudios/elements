package dev.getelements.elements.sdk.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.inject.Scopes.isCircularProxy;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * A generic Guice {@link Scope} which can be used to track an instance of an object using a
 * {@link ReentrantThreadLocal<ScopedT>}. THis manages a proxy and stores the actual scoped objects in an instance of
 * {@link Attributes} for the actual scoped objects.
 *
 * @param <ScopedT>
 */
public class ReentrantThreadLocalScope<ScopedT> implements Scope {

    private final ScopedT proxy;

    private final Key<ScopedT> current;

    private final Class<ScopedT> scoped;

    private final Supplier<ScopedT> instanceSupplier;

    private final Function<ScopedT, MutableAttributes> resolver;

    public ReentrantThreadLocalScope(final Class<ScopedT> scoped,
                                     final ReentrantThreadLocal<ScopedT> instance,
                                     final Function<ScopedT, MutableAttributes> resolver) {
        this(scoped, instance::getCurrent, resolver);
    }

    public ReentrantThreadLocalScope(final Class<ScopedT> scoped,
                                     final Supplier<ScopedT> instanceSupplier,
                                     final Function<ScopedT, MutableAttributes> resolver) {

        this.scoped = scoped;
        this.current = Key.get(scoped);
        this.resolver = resolver;
        this.instanceSupplier = instanceSupplier;

        this.proxy = scoped.cast(newProxyInstance(
            getSystemClassLoader(),
            new Class[]{scoped}, (proxy, method, args) -> {
                final var actual = instanceSupplier.get();
                return method.invoke(actual, args);
            }));
        
    }

    public ScopedT getProxy() {
        return proxy;
    }

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return current.equals(key) ? () -> (T) proxy : resolve(key, unscoped);
    }

    private <T> Provider<T> resolve(final Key<T> key, final Provider<T> unscoped) {
        return () -> {
            final var scoped = instanceSupplier.get();
            final var attributes = resolver.apply(scoped);
            return (T) attributes.getAttributeOptional(key.toString()).orElseGet(() -> {
                final var object = unscoped.get();
                if (!isCircularProxy(object)) attributes.setAttribute(key.toString(), object);
                return object;
            });
        };
    }

    @Override
    public String toString() {
        return format("Scope for %s", scoped.getName());
    }

}
