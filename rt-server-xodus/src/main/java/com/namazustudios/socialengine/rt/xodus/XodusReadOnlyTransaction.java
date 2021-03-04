package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Publisher;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.SimplePublisher;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ReadOnlyTransaction;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.env.Transaction;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class XodusReadOnlyTransaction implements ReadOnlyTransaction {

    private final NodeId nodeId;

    private final XodusResourceStores stores;

    private final Transaction transaction;

    private final Publisher<XodusReadOnlyTransaction> onClose = new SimplePublisher<>();

    public XodusReadOnlyTransaction(final NodeId nodeId,
                                    final XodusResourceStores stores,
                                    final Transaction transaction) {
        this.nodeId = nodeId;
        this.stores = stores;
        this.transaction = transaction;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        check(resourceId);
        final var key = new ArrayByteIterable(resourceId.asBytes());
        return getStores().getReversePaths().get(getTransaction(), key) != null;
    }

    @Override
    public Stream<ResourceService.Listing> list(final Path path) {
        final var qualified = check(path);
        return qualified.isWildcard() ? listWildcard(qualified) : listSingular(qualified);
    }

    private Stream<ResourceService.Listing> listSingular(final Path singular) {
        final var pathKey = XodusUtil.pathKey(singular);
        final var resourceIdValue = getStores().getPaths().get(getTransaction(), pathKey);
        return resourceIdValue == null ? Stream.empty() : Stream.of(new XodusListing(singular, resourceIdValue));
    }

    private Stream<ResourceService.Listing> listWildcard(final Path wildcard) {

        final var pathPrefixKey = XodusUtil.pathKey(check(wildcard.stripWildcard()));
        final var cursor = getStores().getPaths().openCursor(getTransaction());
        final var first = cursor.getSearchKeyRange(pathPrefixKey);
        if (first == null) return Stream.empty();

        final var onCloseSubscription = onClose.subscribe(t -> cursor.close());

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
                    cursor.close();
                    onCloseSubscription.unsubscribe();
                    return false;
                }

            }

        };

        return StreamSupport.stream(spliterator, false);

    }

    @Override
    public ResourceId getResourceId(final Path path) {
        final var qualified = check(path);
        final var key = new ArrayByteIterable(qualified.toByteArray());
        final var value = getStores().getReversePaths().get(getTransaction(), key);
        if (value == null) throw new ResourceNotFoundException();
        return XodusUtil.resourceId(value);
    }

    @Override
    public ReadableByteChannel loadResourceContents(final ResourceId resourceId) {

        check(resourceId);

        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);
        final var cursor = getStores().getResourceBlocks().openCursor(getTransaction());

        final var first = cursor.getSearchKeyRange(resourceIdKey);
        if (first == null) throw new ResourceNotFoundException();

        final var onCloseSubscription = onClose.subscribe(t -> cursor.close());

        return new ReadableByteChannel() {

            boolean open = true;

            ByteBuffer current = XodusUtil.byteBuffer(first);

            @Override
            public int read(final ByteBuffer dst) {

                if (!open) throw new IllegalStateException();

                if (current == null) {
                    return -1;
                } else if (!current.hasRemaining()) {
                    if (cursor.getNext() && XodusUtil.isMatchingBlockKey(resourceIdKey, cursor.getKey())) {
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
                    onCloseSubscription.unsubscribe();
                }
            }

        };

    }

    public XodusResourceStores getStores() {
        return stores;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public void close() {
        onClose.publish(this);
        onClose.clear();
    }

}
