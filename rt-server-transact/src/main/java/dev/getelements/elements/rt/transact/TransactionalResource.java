package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.MethodDispatcher;
import dev.getelements.elements.rt.MutableAttributes;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.id.TaskId;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Set;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public final class TransactionalResource implements Resource {

    private int acquires;

    private final Resource delegate;

    private final TransactionalResourceServiceCache lifecycleOwner;

    /**
     * Creates a new instance of {@link TransactionalResource} with the supplied {@link Runnable} which will execute
     * when the last reference has been released.
     *
     * @param delegate the delegate backs this {@link TransactionalResource}
     */
    public TransactionalResource(final TransactionalResourceServiceCache lifecycleOwner, final Resource delegate) {
        this.acquires = 1;
        this.delegate = delegate;
        this.lifecycleOwner = lifecycleOwner;
        requireNonNull(delegate, "delegate may not be null.");
    }

    @Override
    public ResourceId getId() {
        return delegate.getId();
    }

    @Override
    public MutableAttributes getAttributes() {
        return checkAndGetDelegate().getAttributes();
    }

    @Override
    public MethodDispatcher getMethodDispatcher(String name) {
        return checkAndGetDelegate().getMethodDispatcher(name);
    }

    @Override
    public void resume(TaskId taskId, Object... results) {
        checkAndGetDelegate().resume(taskId, results);
    }

    @Override
    public void resumeFromNetwork(TaskId taskId, Object result) {
        checkAndGetDelegate().resumeFromNetwork(taskId, result);
    }

    @Override
    public void resumeWithError(TaskId taskId, Throwable throwable) {
        checkAndGetDelegate().resumeWithError(taskId, throwable);
    }

    @Override
    public void resumeFromScheduler(TaskId taskId, double elapsedTime) {
        checkAndGetDelegate().resumeFromScheduler(taskId, elapsedTime);
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        checkAndGetDelegate().serialize(os);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        checkAndGetDelegate().deserialize(is);
    }

    @Override
    public void serialize(WritableByteChannel wbc) throws IOException {
        checkAndGetDelegate().serialize(wbc);
    }

    @Override
    public void deserialize(ReadableByteChannel is) throws IOException {
        checkAndGetDelegate().deserialize(is);
    }

    @Override
    public void setVerbose(boolean verbose) {
        checkAndGetDelegate().setVerbose(verbose);
    }

    @Override
    public boolean isVerbose() {
        return checkAndGetDelegate().isVerbose();
    }

    @Override
    public Set<TaskId> getTasks() {
        return checkAndGetDelegate().getTasks();
    }

    @Override
    public Logger getLogger() {
        return checkAndGetDelegate().getLogger();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void unload() {
        delegate.unload();
    }

    public void acquire() {

        if (acquires <= 0) {
            throw new IllegalStateException("Resource is destroyed.");
        }

        ++acquires;

    }

    public int release() {
        return acquires = max(0, acquires - 1);
    }

    public final Resource getDelegate() {
        return delegate;
    }

    private final Resource checkAndGetDelegate() {

        if (acquires <= 0) {
            throw new IllegalStateException("Resource is destroyed.");
        }

        return delegate;

    }

    public boolean isFullyReleased() {
        return acquires <= 0;
    }

    public TransactionalResourceServiceCache getLifecycleOwner() {
        return lifecycleOwner;
    }

}
