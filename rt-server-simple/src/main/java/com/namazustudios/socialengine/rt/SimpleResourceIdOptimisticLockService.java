package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Objects;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static com.namazustudios.socialengine.rt.id.NodeId.forInstanceAndApplication;
import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceIdForNode;
import static java.util.UUID.randomUUID;

/**
 * Created by patricktwohig on 4/11/17.
 */
public class SimpleResourceIdOptimisticLockService implements OptimisticLockService<ResourceId> {

    // This is a uniquely assigned NodeId which, being composed of two random UUIDs, constitutes a fictional
    // Node and Application which should exist nowhere else.  Thereby guaranteeing that only ResourceId instances
    // issued here will ever be visible here.
    private static final NodeId LOCK_NODE_ID = forInstanceAndApplication(randomInstanceId(), randomApplicationId());

    @Override
    public ResourceId createLock() {
        return randomResourceIdForNode(LOCK_NODE_ID);
    }

    @Override
    public boolean isLock(final ResourceId resourceId) {
        return resourceId != null && resourceId.getNodeId().equals(LOCK_NODE_ID);
    }

}
