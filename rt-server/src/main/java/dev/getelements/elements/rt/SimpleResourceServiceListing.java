package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.io.Serializable;
import java.util.Objects;

public class SimpleResourceServiceListing implements ResourceService.Listing, Serializable {

    private Path path;

    private ResourceId resourceId;

    @Override
    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    public void setResourceId(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleResourceServiceListing that = (SimpleResourceServiceListing) o;
        return Objects.equals(getPath(), that.getPath()) && Objects.equals(getResourceId(), that.getResourceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getResourceId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleResourceServiceListing{");
        sb.append("path=").append(path);
        sb.append(", resourceId=").append(resourceId);
        sb.append('}');
        return sb.toString();
    }

}
