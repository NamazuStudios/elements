package com.namazustudios.socialengine.rt.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Provides a delegate type for any interface allowing the underlying implementation to be swapped out as necessary.
 * The swapping of the delegate is thread safe, but the delegated type may not be.
 *
 * @param <DelegateT>
 */
public interface ProxyDelegate<DelegateT> {

    /**
     * Gets the underlying delegated type.
     *
     * @return the delegate, or null
     */
    DelegateT getProxy();

    /**
     * Stops the delegated type, returning the delegate
     * @return
     */
    DelegateT stop();

    /**
     * Starts the delegate with the delegate suppler. This will not invoke the supplier unless the instance is in the
     * state to accept the delegate.
     *
     * @param delegateTSupplier the suppleir
     * @return the delegated type
     * @throws IllegalStateException if there is already a delegate
     */
    DelegateT start(Supplier<DelegateT> delegateTSupplier);

    static <ProxyDelegateT> ProxyDelegate<ProxyDelegateT> getProxy(final Class<ProxyDelegateT> proxyDelegateTClass) {

        final var ref = new AtomicReference<>();

        var delegate = proxyDelegateTClass.cast(newProxyInstance(
            proxyDelegateTClass.getClassLoader(),
            new Class[] { proxyDelegateTClass },
            (proxy, method, args) -> {
                final var d = ref.get();
                if (d == null)
                    throw new IllegalStateException("Not started.");
                return method.invoke(d, args);
            }
        ));

        return new ProxyDelegate<>() {

            @Override
            public ProxyDelegateT getProxy() {
                return delegate;
            }

            @Override
            public ProxyDelegateT stop() {
                final var d = ref.getAndSet(null);
                if (d == null)
                    throw new IllegalStateException("No delegate set.");
                return proxyDelegateTClass.cast(d);
            }

            @Override
            public ProxyDelegateT start(final Supplier<ProxyDelegateT> delegateTSupplier) {
                final var tmp = new Object();
                try {
                    if (ref.compareAndSet(null, tmp) && ref.compareAndSet(tmp, delegateTSupplier.get())) {
                        return delegate;
                    } else {
                        throw new IllegalStateException("Already set.");
                    }
                } finally {
                    ref.compareAndSet(tmp, null);
                }
            }

        };
    }

}
