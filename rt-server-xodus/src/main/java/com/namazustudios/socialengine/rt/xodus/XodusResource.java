package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.SimpleDelegateResource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class XodusResource extends SimpleDelegateResource {

    private static final Logger logger = LoggerFactory.getLogger(XodusResource.class);

    private int acquires = 0;

    private final XodusCacheStorage xodusCacheStorage;

    private final XodusCacheKey xodusCacheKey;

    XodusResource(final Resource delegate, final XodusCacheStorage xodusCacheStorage) {
        super(delegate);
        this.xodusCacheStorage = xodusCacheStorage;
        this.xodusCacheKey = new XodusCacheKey(delegate.getId());
    }

    public XodusCacheKey getXodusCacheKey() {
        return xodusCacheKey;
    }

    public XodusResource acquire() {
        acquires++;
        return this;
    }

    public void release(final Transaction txn, final Store resources) {

        acquires--;

        if (acquires <= 0 && getDelegate().isPersistentState()) {
            if (acquires < 0) logger.error("Unbalanced release/acquire for resource {}", getId());
            persist(txn, resources);
        } else {
            cache();
        }

    }

    public void persist(final Transaction txn, final Store resources) {

        final ByteArrayOutputStream bos;

        try (final ByteArrayOutputStream b = bos = new ByteArrayOutputStream()) {
            getDelegate().serialize(b);
        } catch (IOException e) {
            throw new InternalException("IOException Opening Byte Stream", e);
        }

        final ByteIterable value = new ArrayByteIterable(bos.toByteArray());
        resources.put(txn, getXodusCacheKey().getKey(), value);
        xodusCacheStorage.getResourceIdResourceMap().remove(getXodusCacheKey());

    }

    public void cache() {

        // The resource cannot be persisted at this time as part of lazy persistence.  The resource must be
        // stored in memory until it is ready for persistence.

        final XodusResource existing = xodusCacheStorage.getResourceIdResourceMap().putIfAbsent(getXodusCacheKey(), this);

        if (existing != null && existing != this) {
            logger.error("Resource ID conflict () ({} <-> {})", getId(), this, existing);
        }

    }

    @Override
    public String toString() {
        return "XodusResource{" +
                "acquires=" + acquires +
                ", xodusCacheStorage=" + xodusCacheStorage +
                ", xodusCacheKey=" + xodusCacheKey +
                "} " + super.toString();
    }

}
