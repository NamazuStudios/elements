package dev.getelements.elements.rt.xodus;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.transact.NullResourceException;
import dev.getelements.elements.rt.transact.ReadOnlyTransaction;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Integer.min;

public class XodusReadOnlyTransaction implements ReadOnlyTransaction {

    private final Logger logger = LoggerFactory.getLogger(XodusReadOnlyTransaction.class);

    private boolean open = true;

    private final NodeId nodeId;

    private final VirtualFileSystem virtualFileSystem;

    private final XodusResourceStores stores;

    private final Transaction transaction;

    private final Publisher<XodusReadOnlyTransaction> onClose = new SimplePublisher<>();

    public XodusReadOnlyTransaction(final NodeId nodeId,
                                    final XodusResourceStores stores,
                                    final VirtualFileSystem virtualFileSystem,
                                    final Transaction transaction) {
        this.nodeId = nodeId;
        this.stores = stores;
        this.virtualFileSystem = virtualFileSystem;
        this.transaction = transaction;
        onClose(rot -> logger.debug("Closing {}", rot));
        onClose(rot -> open = false);
        onClose(t -> {
            if (!getTransaction().isFinished()) {
                getTransaction().abort();
            }
        });
    }

    @Override
    public NodeId getNodeId() {
        check();
        return nodeId;
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        check();
        check(resourceId);
        final var key = new ArrayByteIterable(resourceId.asBytes());
        return getStores().getReversePaths().get(getTransaction(), key) != null;
    }

    @Override
    public Stream<ResourceService.Listing> list(final Path path) {
        check();
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
        final var onCloseSubscription = onClose(t -> cursor.close());

        final var first = cursor.getSearchKeyRange(pathPrefixKey);
        if (first == null) return Stream.empty();

        final var spliterator = new Spliterators.AbstractSpliterator<ResourceService.Listing>(Long.MAX_VALUE, 0) {

            boolean more = true;

            @Override
            public boolean tryAdvance(final Consumer<? super ResourceService.Listing> action) {

                if (!more) return cleanup();

                final var pathKey = cursor.getKey();
                final var resourceIdValue = cursor.getValue();
                more = cursor.getNext();

                final var path = XodusUtil.path(pathKey);
                final var resourceId = XodusUtil.resourceId(resourceIdValue);

                if (wildcard.matches(path)) {
                    final var listing = new XodusListing(path, resourceId);
                    action.accept(listing);
                    return true;
                } else {
                    return cleanup();
                }

            }

            private boolean cleanup() {
                cursor.close();
                onCloseSubscription.unsubscribe();
                return false;
            }

        };

        return StreamSupport.stream(spliterator, false);

    }

    @Override
    public ResourceId getResourceId(final Path path) {
        final var qualified = check(path);
        final var pathKey = XodusUtil.pathKey(qualified);
        final var resourceIdKey = getStores().getPaths().get(getTransaction(), pathKey);
        if (resourceIdKey == null) throw new ResourceNotFoundException();
        return XodusUtil.resourceId(resourceIdKey);
    }

    @Override
    public ReadableByteChannel loadResourceContents(final ResourceId resourceId) {

        check(resourceId);

        final var resourceFile = virtualFileSystem.openFile(getTransaction(), resourceId.toString(), false);
        if (resourceFile == null)
                throw new NullResourceException("File for: " + resourceId);

        final var inputStream = virtualFileSystem.readFile(getTransaction(), resourceFile);

        return new ReadableByteChannel() {

            boolean open = true;

            @Override
            public int read(final ByteBuffer dst) {

                if (!open) throw new IllegalStateException();

                byte[] bytes = new byte[dst.remaining()];
                int readBytes = inputStream.read(bytes, 0, bytes.length);

                if(readBytes > 0) {
                    dst.put(bytes, 0, readBytes);
                }

                return readBytes;
            }

            @Override
            public boolean isOpen() {
                return open;
            }

            @Override
            public void close() {
                if (open) {
                    open = false;
                    inputStream.close();
                }
            }

        };

    }

    @Override
    public void close() {
        onClose.publish(this);
        onClose.clear();
        onClose.subscribe(rot -> logger.warn("Transaction already closed {}", rot));
    }

    public void check() {
        if (!open) throw new IllegalStateException("Transaction is closed.");
    }

    public Subscription onClose(final Consumer<ReadOnlyTransaction> readOnlyTransactionConsumer) {
        return onClose.subscribe(readOnlyTransactionConsumer);
    }

    public XodusResourceStores getStores() {
        return stores;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    private void dumpBlocksForResourceId() {
        var cursor = getStores().getResourceBlocks().openCursor(getTransaction());
        while(cursor.getNext()) {
            final var key = cursor.getKey();
            final var value = cursor.getValue().subIterable(0, 10);
            logger.info(String.format("%s : key = %s value = %s", "READ", key, value));
        }
        cursor.close();
    }
}
