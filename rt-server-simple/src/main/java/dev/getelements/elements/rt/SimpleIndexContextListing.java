package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.ResourceId;

import java.io.Serializable;

public class SimpleIndexContextListing implements IndexContext.Listing, Serializable {

    private Path path;

    private ResourceId resourceId;

    public SimpleIndexContextListing() {}

    public SimpleIndexContextListing(final ResourceService.Listing listing) {
        this.path = listing.getPath();
        this.resourceId = listing.getResourceId();
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
