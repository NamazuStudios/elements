package com.namazustudios.socialengine.rt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
