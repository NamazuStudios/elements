package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

public interface ReadWriteTransaction extends ReadOnlyTransaction {

    WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws IOException;

    void linkNewResource(Path path, ResourceId id);

    void linkExistingResource(ResourceId sourceResourceId, Path destination);

    ResourceService.Unlink unlinkPath(Path path);

    List<ResourceService.Unlink> unlinkMultiple(Path path, int max);

    void removeResource(ResourceId resourceId);

    List<ResourceId> removeResources(Path path, int max);

    void commit() throws TransactionConflictException;

}
