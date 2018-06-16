package com.namazustudios.socialengine.rt;

/**
 * A dummy implementation of the {@link ResourceAcquisition} instance.
 */
public class NullResourceAcquisition implements ResourceAcquisition {

    @Override
    public void acquire(ResourceId resourceId) {}

    @Override
    public void release(ResourceId resourceId) {}

}
