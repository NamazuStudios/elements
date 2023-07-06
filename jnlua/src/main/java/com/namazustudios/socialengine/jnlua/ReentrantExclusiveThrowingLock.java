package com.namazustudios.socialengine.jnlua;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantExclusiveThrowingLock implements Lock {

    private final Lock delegate = new ReentrantLock();

    @Override
    public void lock() {
        if (!tryLock()) throw new LockedException();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (!tryLock()) throw new LockedException();
    }

    @Override
    public boolean tryLock() {
        return delegate.tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return delegate.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        delegate.unlock();
    }

    @Override
    public Condition newCondition() {
        return delegate.newCondition();
    }

    public static class LockedException extends IllegalStateException {}

}
