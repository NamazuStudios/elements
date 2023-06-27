package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;

import java.util.Objects;
import java.util.UUID;

import static dev.getelements.elements.rt.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.rt.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.rt.id.NodeId.forInstanceAndApplication;
import static dev.getelements.elements.rt.id.ResourceId.randomResourceIdForNode;
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
