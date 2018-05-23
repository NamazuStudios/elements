package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceId;
import com.namazustudios.socialengine.rt.ResourceService;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.StringBinding;

import java.io.Serializable;
import java.util.Objects;

import static jetbrains.exodus.bindings.StringBinding.entryToString;

class XodusListing implements ResourceService.Listing, Serializable {

    private final Path path;

    private final ResourceId resourceId;

    public XodusListing(final ByteIterable key, final ByteIterable value) {
        this(Path.fromPathString(entryToString(key)), new ResourceId(entryToString(value)));
    }

    public XodusListing(final Path path, final ResourceId resourceId) {
        this.path = path;
        this.resourceId = resourceId;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return "XodusListing{" +
                "path=" + path +
                ", resourceId=" + resourceId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XodusListing)) return false;
        XodusListing that = (XodusListing) o;
        return Objects.equals(getPath(), that.getPath()) &&
                Objects.equals(getResourceId(), that.getResourceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getResourceId());
    }

}
