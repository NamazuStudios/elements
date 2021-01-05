package com.namazustudios.socialengine.rt.guice;

import com.google.common.io.Files;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.MutableAttributes;
import com.namazustudios.socialengine.rt.ReentrantThreadLocal;

import java.util.function.Function;

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

    private final ReentrantThreadLocal<ScopedT> instance;

    private final Function<ScopedT, MutableAttributes> resolver;

    public ReentrantThreadLocalScope(final Class<ScopedT> scoped,
                                     final ReentrantThreadLocal<ScopedT> instance,
                                     final Function<ScopedT, MutableAttributes> resolver) {

        this.scoped = scoped;
        this.current = Key.get(scoped);
        this.instance = instance;
        this.resolver = resolver;

        this.proxy = scoped.cast(newProxyInstance(
            getSystemClassLoader(),
            new Class[]{scoped}, (proxy, method, args) -> {
                final var actual = instance.getCurrent();
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
            final var scoped = instance.getCurrent();
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
