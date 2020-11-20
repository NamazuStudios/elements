package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.ResponseCode;

public class ResourceDestroyedException extends BaseException {

    private final ResourceId resourceId;

    public ResourceDestroyedException(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.RESOURCE_DESTROYED;
    }

}
