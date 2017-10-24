package com.namazustudios.socialengine.rt;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Proxy;
import java.util.AbstractCollection;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.UUID.randomUUID;

public class PathOptimisticLockService implements OptimisticLockService<Deque<Path>> {

    @Override
    public Deque<Path> createLock() {
        return (Deque<Path>) newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {Deque.class, Lock.class},
            (proxy, method, args) -> {
                if (method.getName().equals("equals") && args.length == 1) {
                    return proxy == args[0];
                } else if (method.getName().equals("hashCode") && args == null) {
                    return System.identityHashCode(proxy);
                } else {
                    throw new NotImplementedException();
                }
            });
    }

    @Override
    public boolean isLock(final Deque<Path> candidate) {
        return (candidate instanceof Lock);
    }

    private interface Lock {}

}
