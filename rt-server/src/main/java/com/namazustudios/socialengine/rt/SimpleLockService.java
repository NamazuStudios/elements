package com.namazustudios.socialengine.rt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by patricktwohig on 4/12/17.
 */
public class SimpleLockService implements LockService {

    private final ConcurrentMap<ResourceId, ReentrantLock> resourceIdLockMap = new ConcurrentHashMap<>();

    @Override
    public Lock getLock(final ResourceId resourceId) {
        return resourceIdLockMap.computeIfAbsent(resourceId, rid -> new ReentrantLock());
    }

}
