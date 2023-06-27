package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.id.ResourceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a master record of pessimistically locked resources.
 */
public class SimplePessimisticLockingMaster implements PessimisticLockingMaster {

    private final Map<Object, Object> lockedResources = new ConcurrentHashMap<>();

    @Override
    public PessimisticLocking newPessimisticLocking() {

        return new PessimisticLocking() {

            final List<Object> toRelease = new ArrayList<>();

            private boolean tryLock(final Object object) {

                final Object existing = lockedResources.putIfAbsent(object, this);

                if (existing == null || existing == this) {
                    toRelease.add(object);
                    return true;
                } else {
                    return false;
                }

            }

            @Override
            public boolean tryLock(final dev.getelements.elements.rt.Path path) {
                return tryLock((Object) path);
            }

            @Override
            public boolean tryLock(final ResourceId resourceId) {
                return tryLock((Object) resourceId);
            }

            @Override
            public void unlock() {
                lockedResources.keySet().removeAll(toRelease);
            }

            @Override
            public boolean unlock(final dev.getelements.elements.rt.Path rtPath) {
                return lockedResources.remove(rtPath, this);
            }

            @Override
            public boolean unlock(final ResourceId resourceId) {
                return lockedResources.remove(resourceId, this);
            }

        };
    }

}
