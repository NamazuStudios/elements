package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.io.Serializable;
import java.util.Objects;

public class SimpleResourceServiceUnlink implements ResourceService.Unlink, Serializable {

    public static SimpleResourceServiceUnlink from(final ResourceId resourceId, boolean removed) {
        final var unlink = new SimpleResourceServiceUnlink();
        unlink.setRemoved(removed);
        unlink.setResourceId(resourceId);
        return unlink;
    }

    private boolean removed;

    private ResourceId resourceId;

    @Override
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
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
        SimpleResourceServiceUnlink that = (SimpleResourceServiceUnlink) o;
        return isRemoved() == that.isRemoved() && Objects.equals(getResourceId(), that.getResourceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRemoved(), getResourceId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleResourceServiceUnlink{");
        sb.append("removed=").append(removed);
        sb.append(", resourceId=").append(resourceId);
        sb.append('}');
        return sb.toString();
    }

}
