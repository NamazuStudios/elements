package com.namazustudios.socialengine.rt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by patricktwohig on 4/12/17.
 */
public class SimpleResourceLockService implements ResourceLockService {

    private final ConcurrentMap<ResourceId, ReentrantLock> resourceIdLockMap = new ConcurrentHashMap<>();

    @Override
    public Monitor getMonitor(ResourceId resourceId) {

        final Lock lock = resourceIdLockMap.computeIfAbsent(resourceId, rid -> new ReentrantLock());
        lock.lock();

        return new Monitor() {

            final Map<String, Condition> stringConditionMap = new ConcurrentHashMap<>();

            @Override
            public Condition getCondition(final String name) {
                return stringConditionMap.computeIfAbsent(name, k -> lock.newCondition());
            }

            @Override
            public void close() {
                lock.unlock();
            }

        };

    }

    @Override
    public void delete(ResourceId resourceId) {
        resourceIdLockMap.remove(resourceId);
    }

}
