package com.namazustudios.socialengine.rt;

import java.util.Objects;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Created by patricktwohig on 4/11/17.
 */
public class SimpleResourceIdOptimisticLockService implements OptimisticLockService<ResourceId> {

    private final UUID lockUuid = randomUUID();

    @Override
    public ResourceId createLock() {
        return new LockingId(lockUuid);
    }

    @Override
    public boolean isLock(ResourceId resourceId) {
        return resourceId != null && Objects.equals(LockingId.class, resourceId.getClass());
    }

    private static class LockingId extends ResourceId {
        public LockingId(UUID nodeUuid) {
            super(nodeUuid);
        }
    }

}
