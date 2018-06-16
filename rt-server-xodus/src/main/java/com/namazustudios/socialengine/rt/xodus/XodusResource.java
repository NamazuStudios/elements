package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.SimpleDelegateResource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class XodusResource extends SimpleDelegateResource {

    private final XodusCacheKey xodusCacheKey;

    XodusResource(final Resource delegate) {
        super(delegate);
        this.xodusCacheKey = new XodusCacheKey(delegate.getId());
    }

    XodusCacheKey getXodusCacheKey() {
        return xodusCacheKey;
    }

    void persist(final Transaction txn, final Store resources) {

        final ByteArrayOutputStream bos;

        try (final ByteArrayOutputStream b = bos = new ByteArrayOutputStream()) {
            getDelegate().serialize(b);
        } catch (IOException e) {
            throw new InternalException("IOException Serializing Resource.", e);
        }

        final ByteIterable value = new ArrayByteIterable(bos.toByteArray());
        resources.put(txn, getXodusCacheKey().getKey(), value);

    }

    @Override
    public String toString() {
        return "XodusResource{" +
                ", xodusCacheKey=" + xodusCacheKey +
                "} " + super.toString();
    }

}
