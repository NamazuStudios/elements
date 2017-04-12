package com.namazustudios.socialengine.rt;

import java.util.Objects;

/**
 * Created by patricktwohig on 4/11/17.
 */
public class SimplePathLockFactory implements PathLockFactory {

    @Override
    public ResourceId createLock() {
        return new LockingId();
    }

    @Override
    public boolean isLock(ResourceId resourceId) {
        return Objects.equals(LockingId.class, resourceId.getClass());
    }

    private static class LockingId extends ResourceId {}

}
