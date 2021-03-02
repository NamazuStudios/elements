package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ReadOnlyTransaction;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.env.Transaction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

public class XodusReadOnlyTransaction implements ReadOnlyTransaction {

    private final ResourceStores stores;

    private final Transaction transaction;

    public XodusReadOnlyTransaction(final ResourceStores stores, final Transaction transaction) {
        this.stores = stores;
        this.transaction = transaction;
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        final var key = new ArrayByteIterable(resourceId.asBytes());
        return stores.getReversePaths().get(transaction, key) != null;
    }

    @Override
    public Stream<ResourceService.Listing> list(final Path path) {
        return null;
    }

    @Override
    public ResourceId getResourceId(final Path path) {
        final var key = new ArrayByteIterable(path.toByteArray());
        final var value = stores.getReversePaths().get(transaction, key);
        if (value == null) throw new ResourceNotFoundException();
        return XodusUtil.resourceId(value);
    }

    @Override
    public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {

        final var key = new ArrayByteIterable(resourceId.asBytes());
        final var cursor = stores.getResources().openCursor(transaction);

        final var first = cursor.getSearchKey(key);
        if (first == null) throw new ResourceNotFoundException();

        return new ReadableByteChannel() {

            boolean open = true;

            ByteBuffer current = XodusUtil.byteBuffer(first);

            @Override
            public int read(final ByteBuffer dst) {

                if (current == null) {
                    return -1;
                } else if (!current.hasRemaining()) {
                    if (cursor.getNextDup()) {
                        current = XodusUtil.byteBuffer(cursor.getValue());
                    } else {
                        current = null;
                        return -1;
                    }
                }

                final var initial = current.remaining();
                dst.put(current);

                return initial - current.remaining();

            }

            @Override
            public boolean isOpen() {
                return open;
            }

            @Override
            public void close() {
                if (open) {
                    open = false;
                    cursor.close();
                }
            }

        };

    }

    @Override
    public void close() {}

}
