package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.ResourceService;
import jetbrains.exodus.ByteIterable;

import java.io.Serializable;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromString;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

class XodusListing implements ResourceService.Listing, Serializable {

    private final Path path;

    private final ResourceId resourceId;

    private transient volatile ByteIterable pathKey;

    private transient volatile ByteIterable resourceIdValue;

    public XodusListing(final ByteIterable pathKey, final ByteIterable resourceIdValue) {
        this(Path.fromPathString(entryToString(pathKey)), resourceIdFromString(entryToString(resourceIdValue)));
        this.pathKey = pathKey;
        this.resourceIdValue = resourceIdValue;
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

    public ByteIterable getPathKey() {
        return pathKey == null ? (pathKey = stringToEntry(path.toAbsolutePathString())) : pathKey;
    }

    public ByteIterable getResourceIdValue() {
        return resourceIdValue == null ? (resourceIdValue = stringToEntry(resourceId.asString())) : resourceIdValue;
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
