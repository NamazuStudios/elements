package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.ResourceEntry;

import java.util.Optional;

public class UnixFSResourceEntryNew extends UnixFSResourceEntryBase {

    private final ResourceId resourceId;

    public UnixFSResourceEntryNew(
            final UnixFSUtils utils,
            final ResourceId resourceId,
            final ResourceEntry.OperationalStrategy operationalStrategy) {
        super(operationalStrategy, utils);
        this.resourceId = resourceId;
    }

    @Override
    public ResourceId getOriginalResourceId() {
        return resourceId;
    }

    @Override
    public Optional<ResourceId> findOriginalResourceId() {
        return Optional.of(resourceId);
    }

}
