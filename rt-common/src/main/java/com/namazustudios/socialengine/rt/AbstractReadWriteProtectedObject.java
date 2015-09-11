package com.namazustudios.socialengine.rt;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by patricktwohig on 9/11/15.
 */
public abstract class AbstractReadWriteProtectedObject<ProtectedT> implements ReadWriteProtectedObject<ProtectedT> {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public <ReturnT> ReturnT read(CriticalSection<ReturnT, ProtectedT> criticalSection) {
        try (final Monitor<ProtectedT> monitor = read()){
            return criticalSection.perform(monitor.get());
        }
    }

    @Override
    public <ReturnT> ReturnT write(CriticalSection<ReturnT, ProtectedT> criticalSection) {
        try (final Monitor<ProtectedT> monitor = write()){
            return criticalSection.perform(monitor.get());
        }
    }

    @Override
    public Monitor<ProtectedT> read() {

        final Lock readLock = readWriteLock.readLock();
        readLock.lock();

        return new Monitor<ProtectedT>() {

            boolean open = true;

            @Override
            public ProtectedT get() {
                return immutableView();
            }

            @Override
            public void close() {
                if (open) readLock.unlock();;
            }

        };

    }

    @Override
    public Monitor<ProtectedT> write() {

        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();

        return new Monitor<ProtectedT>() {

            boolean open = true;

            @Override
            public ProtectedT get() {
                return mutableView();
            }

            @Override
            public void close() {
                if (open) writeLock.unlock();;
            }

        };

    }

    /**
     * Provides the mutable view of the protected type.
     *
     * @return the mutable view
     */
    protected abstract ProtectedT mutableView();

    /**
     * Provides the immutable view of the protected type.
     *
     * @return the immutable view
     */
    protected abstract ProtectedT immutableView();

}
