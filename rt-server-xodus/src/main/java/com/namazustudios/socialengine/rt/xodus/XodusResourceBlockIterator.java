package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.namazustudios.socialengine.rt.xodus.XodusUtil.isMatchingBlockKey;

public class XodusResourceBlockIterator implements Iterator<XodusResourceBlock>, AutoCloseable {

    private Cursor cursor;

    private ByteIterable key;

    private final ByteIterable resourceIdKey;

    public XodusResourceBlockIterator(final ResourceId resourceId, final Cursor cursor) {

        this.cursor = cursor;
        this.resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        if (cursor.getSearchKeyRange(resourceIdKey) == null) {
            this.cursor = null;
            cursor.close();
        } else {
            this.cursor = cursor;
        }

    }

    @Override
    public boolean hasNext() {
        return cursor != null;
    }

    @Override
    public XodusResourceBlock next() {
        if (cursor == null) throw new NoSuchElementException();
        final var result = new XodusResourceBlock(cursor.getKey(), cursor.getValue());
        advance();
        return result;
    }

    private void advance() {
        if (!cursor.getNext() || !XodusUtil.isMatchingBlockKey(resourceIdKey, cursor.getKey())) {
            cursor.close();
            cursor = null;
        }
    }

    @Override
    public void remove() {
        if (!cursor.deleteCurrent()) throw new UnsupportedOperationException();
        advance();
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

}
