package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.rt.transact.TransactionalTask;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public class UnixFSTaskEntryNew extends UnixFSTaskEntryBase {

    private final ResourceId resourceId;

    public UnixFSTaskEntryNew(final OperationalStrategy<ResourceId> operationalStrategy,
                              final UnixFSUtils unixFSUtils,
                              final ResourceId resourceId) {
        super(operationalStrategy, unixFSUtils);
        this.resourceId = resourceId;
    }

    @Override
    public Optional<ResourceId> findOriginalScope() {
        return Optional.of(resourceId);
    }

    @Override
    public Map<TaskId, TransactionalTask> getOriginalTasksImmutable() {
        return emptyMap();
    }

    @Override
    public void close() {}

}
