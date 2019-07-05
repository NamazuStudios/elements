package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.ResourceId;

/**
 * A dummy implementation of the {@link ResourceAcquisition} instance.
 */
public class NullResourceAcquisition implements ResourceAcquisition {

    @Override
    public void acquire(ResourceId resourceId) {}

    @Override
    public void scheduleRelease(ResourceId resourceId) {}

}
