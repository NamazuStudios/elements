package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.MethodDispatcher;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceId;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.exception.InternalException;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class XodusResource implements Resource {

    private static final Logger logger = LoggerFactory.getLogger(XodusResource.class);

    private int acquires = 0;

    private final XodusCacheStorage xodusCacheStorage;

    private final Resource delegate;

    private final XodusCacheKey xodusCacheKey;

    XodusResource(final Resource delegate, final XodusCacheStorage xodusCacheStorage) {
        this.delegate = delegate;
        this.xodusCacheStorage = xodusCacheStorage;
        this.xodusCacheKey = new XodusCacheKey(delegate.getId());
    }

    @Override
    public ResourceId getId() {
        return delegate.getId();
    }

    public XodusCacheKey getXodusCacheKey() {
        return xodusCacheKey;
    }

    public Resource getDelegate() {
        return delegate;
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
        return "XodusResource{" +
                "delegate=" + delegate +
                '}';
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Cannot close managed XodusResource");
    }

    public XodusResource acquire() {
        acquires++;
        return this;
    }

    public void release(final Transaction txn, Store resources) {

        acquires--;

        if (acquires <= 0 && delegate.isPersistentState()) {
            if (acquires < 0) logger.error("Unbalanced release/acquire for resource {}", getId());
            persist(txn, resources);
        } else {
            cache();
        }

    }

    private void persist(final Transaction txn, final Store resources) {

        final ByteArrayOutputStream bos;

        try (final ByteArrayOutputStream b = bos = new ByteArrayOutputStream()) {
            delegate.serialize(b);
        } catch (IOException e) {
            throw new InternalException("IOException Opening Byte Stream", e);
        }

        final ByteIterable value = new ArrayByteIterable(bos.toByteArray());
        resources.put(txn, getXodusCacheKey().getKey(), value);
        xodusCacheStorage.getResourceIdResourceMap().remove(getXodusCacheKey());

    }

    private void cache() {

        // The resource cannot be persisted at this time as part of lazy persistence.  The resource must be
        // stored in memory until it is ready for persistence.

        final XodusResource existing = xodusCacheStorage.getResourceIdResourceMap().putIfAbsent(getXodusCacheKey(), this);

        if (existing != null && existing != this) {
            logger.error("Resource ID conflict () ({} <-> {})", getId(), this, existing);
        }

    }

}
