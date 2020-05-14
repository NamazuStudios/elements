package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import com.namazustudios.socialengine.rt.util.LazyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.util.stream.StreamSupport.stream;

class UnixFSJournalEntry implements TransactionJournal.Entry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalEntry.class);

    protected boolean open = true;

    protected final Revision<?> revision;

    protected final UnixFSUtils.IOOperationV onClose;

//    protected final LazyValue<SortedMap<Path, ResourceId>> pathMap;

    public UnixFSJournalEntry(final Revision revision,
                              final UnixFSUtils.IOOperationV onClose) {
        this.revision = revision;
        this.onClose = onClose;
//        this.pathMap = new LazyValue<>(TreeMap::new);
    }

    protected void check() {
        if (!open) throw new IllegalStateException();
    }

    @Override
    public Revision<?> getRevision() {
        check();
        return revision;
    }

    @Override
    public void close() {
        if (open) {
            try {
                open = false;
                onClose.perform();
            } catch (IOException ex) {
                logger.error("Caught IOException closing entry.", ex);
            }
        }
    }

//    @Override
//    public Revision<Boolean> exists(final ResourceId resourceId) {
//        check();
//
//        final Optional<Boolean> optionalBoolean =  pathMap
//            .getOptional()
//            .map(v -> v.containsKey(resourceId));
//
//        return revision.withOptionalValue(optionalBoolean);
//
//    }
//
//    @Override
//    public Revision<Stream<ResourceService.Listing>> list(final Path path) {
//        check();
//
//        final Stream<ResourceService.Listing> listingStream;
//
//        if (path.isWildcard()) {
//            listingStream = listMultiple(path);
//        } else {
//            listingStream = listSingular(path);
//        }
//
//        return revision.withValue(listingStream);
//
//    }
//
//    private Stream<ResourceService.Listing> listSingular(final Path path) {
//        return pathMap
//            .getOptional()
//            .flatMap(map -> Optional.ofNullable(map.get(path)))
//            .map(resourceId -> new UnixFSEntryListing(path, resourceId))
//            .map(ResourceService.Listing.class::cast)
//            .map(Stream::of)
//            .orElseGet(Stream::empty);
//    }
//
//    private Stream<ResourceService.Listing> listMultiple(final Path path) {
//        final Path first = path.stripWildcard();
//        return pathMap.getOptional().map(map -> stream(
//                new Spliterators.AbstractSpliterator<ResourceService.Listing>(MAX_VALUE, 0) {
//
//                    final Iterator<Map.Entry<Path, ResourceId>> iterator = map
//                        .headMap(first)
//                        .entrySet()
//                        .iterator();
//
//                    @Override
//                    public boolean tryAdvance(final Consumer<? super ResourceService.Listing> consumer) {
//
//                        // Check we actually can find the value.
//                        if (!iterator.hasNext()) return false;
//
//                        // Check that the current entry matches the original wildcard path
//                        final Map.Entry<Path, ResourceId> current = iterator.next();
//                        if (!path.matches(current.getKey())) return false;
//
//                        // Finally if both tests pass, then we can make the entry and supply it ot the spliterator
//                        consumer.accept(new UnixFSEntryListing(current));
//                        return true;
//
//                    }
//
//                }, false)
//        ).orElseGet(Stream::empty);
//    }
//
//    @Override
//    public Revision<ResourceId> getResourceId(final Path path) {
//        check();
//        final Optional<ResourceId> resourceIdOptional = pathMap.getOptional().map(m -> m.get(path));
//        return revision.withOptionalValue(resourceIdOptional);
//    }
//
//    @Override
//    public Revision<ReadableByteChannel> loadResourceContents(final Path path) throws IOException {
//        check();
//        return revision.withOptionalValue(Optional.empty());
//    }
//
//    @Override
//    public Revision<ReadableByteChannel> loadResourceContents(final ResourceId resourceId) throws IOException {
//        check();
//        return revision.withOptionalValue(Optional.empty());
//    }

}
