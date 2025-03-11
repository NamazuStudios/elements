package dev.getelements.elements.rt.exception;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.ResponseCode;

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
