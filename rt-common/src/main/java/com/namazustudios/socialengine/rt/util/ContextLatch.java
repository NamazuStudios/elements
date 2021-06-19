package com.namazustudios.socialengine.rt.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Similar to a {@link CountDownLatch}. However, instad of tracking a simple count, this will store a collection of
 * objects which must all finish in order for the operation to complete.
 */
public class ContextLatch {

    private final Set<Object> contexts = new HashSet<>();

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    public <U> ContextLatch(final Collection<U> contexts) {
        this.contexts.addAll(contexts);
    }

    public boolean finish(final Object context) {
        try {
            lock.lock();
            condition.signalAll();
            return contexts.remove(context);
        } finally {
            lock.unlock();
        }
    }

    public void awaitFinish() throws InterruptedException {
        try {
            lock.lock();
            condition.signalAll();
            while (!contexts.isEmpty()) condition.await();
        } finally {
            lock.unlock();
        }
    }

    public boolean awaitFinish(final long time, final TimeUnit timeUnit) throws InterruptedException {

        var remaining = timeUnit.toNanos(time);

        try {

            lock.lock();

            while (!contexts.isEmpty()) {
                remaining = condition.awaitNanos(remaining);
                if (remaining < 0) return false;
            }

            return true;

        } finally {
            lock.unlock();
        }
    }

}
