package dev.getelements.elements.rt.xodus;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.id.ResourceId;
import jetbrains.exodus.ByteIterable;

public class XodusListing implements ResourceService.Listing {

    private final Path path;

    private final ResourceId resourceId;

    public XodusListing(final Path path, final ResourceId resourceId) {
        this.path = path;
        this.resourceId = resourceId;
    }

    public XodusListing(final Path path, final ByteIterable resourceId) {
        this.path = path;
        this.resourceId = XodusUtil.resourceId(resourceId);
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
