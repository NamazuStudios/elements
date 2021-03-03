package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Transaction;

import java.util.Iterator;

public class XodusResourceBlock {

    private final ByteIterable key;

    private final ByteIterable value;

    public XodusResourceBlock(final ByteIterable key, final ByteIterable value) {
        this.key = key;
        this.value = value;
    }

    public ByteIterable getKey() {
        return key;
    }

    public ByteIterable getValue() {
        return value;
    }

    public class BlockIterator implements Iterator<XodusResourceBlock>, AutoCloseable {

        private final Cursor cursor;

        private BlockIterator(final ResourceId resourceId,
                              final ResourceStores stores,
                              final Transaction transaction) {

        cursor = stores.getResourceBlocks().openCursor(transaction);

        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        final var first = cursor.getSearchKeyRange(resourceIdKey);
        if (first == null) throw new ResourceNotFoundException("Resource not found: " + resourceId);

        }


        return new Iterator<>() {

            ByteIterable key = resourceBlockCursor.getKey();

            ByteIterable value = resourceBlockCursor.getValue();

            @Override
            public boolean hasNext() {
                return XodusUtil.isMatchingBlockKey(key, value);
            }

            @Override
            public XodusResourceBlock next() {


                return null;
            }

            @Override
            public void remove() {

            }

        };

    }

}
