package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.DeadResourceException;
import com.namazustudios.socialengine.rt.exception.ResourceDestroyedException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * A singleton {@link Resource} implementation that just throws {@link DeadResourceException} for any method that's
 * invoked (except {@link #close()} as per the {@link AutoCloseable} specification).  It is used as a placeholder in
 * scenarios where a {@link Resource} may be returned or handed to a consumer specifically where it is meant to be
 * closed.  It could also be used as a failsafe detect errors when a managed {@link Resource} has been closed.
 */
public class DeadResource implements Resource {

    private static DeadResource instance = new DeadResource();

    public static DeadResource getInstance() {
        return instance;
    }

    private DeadResource() {}

    @Override
    public ResourceId getId() {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public Attributes getAttributes() {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public MethodDispatcher getMethodDispatcher(String name) {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public void resume(TaskId taskId, Object... results) {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public void serialize(OutputStream os) {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public void deserialize(InputStream is) {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public Set<TaskId> getTasks() {
        throw new DeadResourceException("resource is closed");
    }

    @Override
    public void close() {}

    @Override
    public void unload() {}

}
