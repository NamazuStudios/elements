package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.id.ResourceId;
import jetbrains.exodus.ByteIterable;

import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

class XodusCacheKey {

    private final ByteIterable key;

    private final ResourceId resourceId;

    public XodusCacheKey(final ResourceId resourceId) {
        this.resourceId = resourceId;
        this.key = stringToEntry(resourceId.asString());
    }

    public XodusCacheKey(final ByteIterable key) {
        this.key = key;
        this.resourceId = new ResourceId(entryToString(key));
    }

    public ByteIterable getKey() {
        return key;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XodusCacheKey)) return false;
        final XodusCacheKey that = (XodusCacheKey) o;
        return getResourceId().equals(that.resourceId);
    }

    @Override
    public int hashCode() {
        return getResourceId().hashCode();
    }

}
