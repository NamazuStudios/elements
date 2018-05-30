package com.namazustudios.socialengine.rt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A singleton {@link Resource} implementation that just throws {@link IllegalStateException} for any method that's
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
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public Attributes getAttributes() {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public MethodDispatcher getMethodDispatcher(String name) {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public void resumeFromNetwork(TaskId taskId, Object result) {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public void resumeWithError(TaskId taskId, Throwable throwable) {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public void resumeFromScheduler(TaskId taskId, double elapsedTime) {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        throw new IllegalStateException("resource is closed");
    }

    @Override
    public void close() {}

}
