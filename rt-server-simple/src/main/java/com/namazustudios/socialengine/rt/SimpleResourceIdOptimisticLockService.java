package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Objects;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static java.util.UUID.randomUUID;

/**
 * Created by patricktwohig on 4/11/17.
 */
public class SimpleResourceIdOptimisticLockService implements OptimisticLockService<ResourceId> {

    private static final NodeId MOCK_NODE_ID = new NodeId(randomInstanceId(), randomApplicationId());

    @Override
    public ResourceId createLock() {
        return new LockingId();
    }

    @Override
    public boolean isLock(final ResourceId resourceId) {
        return resourceId != null && Objects.equals(LockingId.class, resourceId.getClass());
    }

    private static class LockingId extends ResourceId {
        public LockingId() {
            super(MOCK_NODE_ID);
        }
    }

}
