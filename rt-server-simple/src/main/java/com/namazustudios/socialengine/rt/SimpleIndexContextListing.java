package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.Serializable;

public class SimpleIndexContextListing implements IndexContext.Listing, Serializable {

    private final Path path;

    private final ResourceId resourceId;

    public SimpleIndexContextListing(final ResourceService.Listing listing) {
        this.path = listing.getPath();
        this.resourceId = listing.getResourceId();
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public ResourceId getResourceId() {
        return null;
    }

}
