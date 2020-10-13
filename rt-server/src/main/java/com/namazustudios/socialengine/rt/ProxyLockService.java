package com.namazustudios.socialengine.rt;

import static java.lang.reflect.Proxy.newProxyInstance;

public class ProxyLockService<LockT> implements OptimisticLockService<LockT> {

    private final Class<LockT> tClass;

    public ProxyLockService(final Class<?> tClass) {

        this.tClass = (Class<LockT>)tClass;

        if (!tClass.isInterface()) {
            throw new IllegalArgumentException(tClass.getName() + " is not an interface.");
        }

    }

    @Override
    public LockT createLock() {
        return (LockT) newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {tClass, Lock.class},
            (proxy, method, args) -> {
                if ("equals".equals(method.getName()) && args.length == 1) {
                    return proxy == args[0];
                } else if ("hashCode".equals(method.getName()) && args == null) {
                    return System.identityHashCode(proxy);
                } else if ("toString".equals(method.getName()) && args == null) {
                    return "Proxy Lock for " + tClass;
                } else {
                    throw new UnsupportedOperationException("not implemented");
                }
            });
    }

    @Override
    public boolean isLock(final LockT candidate) {
        return (candidate instanceof Lock);
    }

    private interface Lock {}

}
