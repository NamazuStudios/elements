package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ReadOnlyTransaction;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.io.Block;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        return path.isWildcard() ? listWildcard(path) : listSingular(path);
    }

    private Stream<ResourceService.Listing> listSingular(final Path path) {
        final var pathKey = XodusUtil.pathKey(path);
        final var resourceIdValue = stores.getPaths().get(transaction, pathKey);
        if (resourceIdValue == null) throw new ResourceNotFoundException("No resource at path: " + path);
        return Stream.of(new XodusListing(path, resourceIdValue));
    }

    private Stream<ResourceService.Listing> listWildcard(final Path wildcard) {

        final var pathPrefixKey = XodusUtil.pathKey(wildcard.stripWildcard());
        final var cursor = stores.getPaths().openCursor(transaction);
        final var first = cursor.getSearchKeyRange(pathPrefixKey);
        if (first == null) return Stream.empty();

        final var spliterator = new Spliterators.AbstractSpliterator<ResourceService.Listing>(Long.MAX_VALUE, 0) {

            @Override
            public boolean tryAdvance(final Consumer<? super ResourceService.Listing> action) {

                final var pathKey = cursor.getKey();
                final var resourceIdValue = cursor.getValue();

                final var path = XodusUtil.path(pathKey);
                final var resourceId = XodusUtil.resourceId(resourceIdValue);

                if (wildcard.matches(path)) {
                    final var listing = new XodusListing(path, resourceId);
                    action.accept(listing);
                    return true;
                } else {
                    return false;
                }

            }

        };

        return StreamSupport.stream(spliterator, false);

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

        final var key = XodusUtil.resourceIdKey(resourceId);
        final var cursor = stores.getResourceBlocks().openCursor(transaction);

        final var first = cursor.getSearchKeyRange(key);
        if (first == null) throw new ResourceNotFoundException();

        return new ReadableByteChannel() {

            boolean open = true;

            ByteBuffer current = XodusUtil.byteBuffer(first);

            @Override
            public int read(final ByteBuffer dst) {

                if (current == null) {
                    return -1;
                } else if (!current.hasRemaining()) {
                    if (cursor.getNext()) {
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
