package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Map;

class UnixFSEntryListing implements ResourceService.Listing {

    private final Path path;

    private final ResourceId resourceId;

    public UnixFSEntryListing(final Path path, final ResourceId resourceId) {
        this.path = path;
        this.resourceId = resourceId;
    }

    public UnixFSEntryListing(final Map.Entry<Path, ResourceId> current) {
        this(current.getKey(), current.getValue());
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

}
