package com.namazustudios.socialengine.rt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of {@link Resource} which delegates all of its responsibility to another instance for actual
 * processing.  This assists in the implementation of the acquire and release semantics of the {@link ResourceService}
 * specified in the {@link ResourceService#release(Resource)},
 * {@link ResourceService#getAndAcquireResourceAtPath(Path)}, and related methods by providing a wrapper around the
 * actual implementation {@link Resource}.
 */
public class SimpleDelegateResource implements Resource {

    private final Resource delegate;

    public SimpleDelegateResource(final Resource delegate) {
        this.delegate = delegate;
    }

    public Resource getDelegate() {
        return delegate;
    }

    @Override
    public ResourceId getId() {
        return delegate.getId();
    }

    @Override
    public Attributes getAttributes() {
        return getDelegate().getAttributes();
    }

    @Override
    public MethodDispatcher getMethodDispatcher(String name) {
        return delegate.getMethodDispatcher(name);
    }

    @Override
    public void resumeFromNetwork(TaskId taskId, Object result) {
        delegate.resumeFromNetwork(taskId, result);
    }

    @Override
    public void resumeWithError(TaskId taskId, Throwable throwable) {
        delegate.resumeWithError(taskId, throwable);
    }

    @Override
    public void resumeFromScheduler(TaskId taskId, double elapsedTime) {
        delegate.resumeFromScheduler(taskId, elapsedTime);
    }

    @Override
    public boolean isPersistentState() {
        return delegate.isPersistentState();
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        delegate.serialize(os);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        delegate.deserialize(is);
    }

    @Override
    public void setVerbose(boolean verbose) {
        delegate.setVerbose(verbose);
    }

    @Override
    public boolean isVerbose() {
        return delegate.isVerbose();
    }

    @Override
    public String toString() {
        return "SimpleDelegateResource{" +
                "delegate=" + delegate +
                '}';
    }

    @Override
    public void close() {
        delegate.close();
    }

}
