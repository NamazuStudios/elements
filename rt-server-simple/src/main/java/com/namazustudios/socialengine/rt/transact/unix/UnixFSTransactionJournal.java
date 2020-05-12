package com.namazustudios.socialengine.rt.transact.unix;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.namazustudios.socialengine.rt.Monitor;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import com.namazustudios.socialengine.rt.util.LazyValue;
import com.namazustudios.socialengine.rt.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.stream.StreamSupport.stream;

public class UnixFSTransactionJournal implements TransactionJournal {

    private static final byte FILLER = (byte) 0xFF;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionJournal.class);

    /**
     * The path to the journal file, specified as a constructor parameter.
     */
    public static final String JOURNAL_PATH = "com.namazustudios.socialengine.rt.transact.journal.path";

    /**
     * The size of each transaction entry.  This is a fixed size.  If a transaction attempts to write more bytes than
     * the size allows, then an exception will result.
     */
    public static final String TRANSACTION_ENTRY_BUFFER_SIZE = "com.namazustudios.socialengine.rt.transact.journal.buffer.size";

    /**
     * The journal buffer count.  This should be large enough to accommodate all running transactions.  The larger this
     * number the better as it will give the journal sufficient breathing room for processing transactions.
     */
    public static final String TRANSACTION_ENTRY_BUFFER_COUNT = "com.namazustudios.socialengine.rt.transact.journal.buffer.count";

    /**
     * Some magic bytes int he file to indicate what it is.
     */
    public static final String JOURNAL_MAGIC = "JELM";

    /**
     * Constant for major version 1
     */
    public static final int VERSION_MAJOR_1 = 1;

    /**
     * Constant for minor version 0
     */
    public static final int VERSION_MINOR_0 = 0;

    /**
     * Indicates the current major version.
     */
    public static final int VERSION_MAJOR_CURRENT = VERSION_MAJOR_1;

    /**
     * Indicates the current minor version.
     */
    public static final int VERSION_MINOR_CURRENT = VERSION_MINOR_0;

    // Created during init

    private final UnixFSJournalHeader header = new UnixFSJournalHeader();

    private final AtomicLong current = new AtomicLong();

    private final Map<Object, MutableEntry> lockedResources = new ConcurrentHashMap<>();

    // Set in constructor

    private final Lock rLock;

    private final Lock wLock;

    private final java.nio.file.Path lockFilePath;

    private final MappedByteBuffer journalBuffer;

    private final long txnEntryBufferSize;

    private final long txnEntryBufferCount;

    // Injected Fields

    private final UnixFSUtils utils;

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSGarbageCollector unixFSGarbageCollector;

    private final Provider<UnixFSTransactionProgram.Builder> programBuilderProvider;

    @Inject
    public UnixFSTransactionJournal(
            final UnixFSUtils utils,
            final UnixFSRevisionPool unixFSRevisionPool,
            final UnixFSGarbageCollector unixFSGarbageCollector,
            final Provider<UnixFSTransactionProgram.Builder> programBuilderProvider,
            @Named(TRANSACTION_ENTRY_BUFFER_SIZE) final int txnEntryBufferSize,
            @Named(TRANSACTION_ENTRY_BUFFER_COUNT) final int txnEntryBufferCount) throws IOException {

        this.utils = utils;
        this.unixFSRevisionPool = unixFSRevisionPool;
        this.unixFSGarbageCollector = unixFSGarbageCollector;
        this.txnEntryBufferSize = txnEntryBufferSize;
        this.txnEntryBufferCount = txnEntryBufferCount;
        this.programBuilderProvider = programBuilderProvider;

        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.rLock = rwLock.readLock();
        this.wLock = rwLock.readLock();

        final java.nio.file.Path journalPath = utils.getJournalPath();
        lockFilePath = this.utils.lockPath(journalPath);

        if (isRegularFile(journalPath)) {
            logger.info("Reading existing journal file {}", journalPath);
            // TODO Read and Recover Journal if Necessary
            throw new UnsupportedOperationException("Not yet implemented.");
        } else {
            journalBuffer = createNewJournal(journalPath);
        }

    }

    private MappedByteBuffer createNewJournal(final java.nio.file.Path journalPath) throws IOException {

        logger.info("Creating new journal file at {}", journalPath);

        try (final FileChannel channel = FileChannel.open(journalPath, READ, WRITE, CREATE)) {

            final long headerSize = header.size();
            final long totalEntrySize = (txnEntryBufferSize * txnEntryBufferCount);

            final ByteBuffer fillHeader = ByteBuffer.allocate(header.size());
            while(fillHeader.hasRemaining()) fillHeader.put(FILLER);
            fillHeader.rewind();
            channel.write(fillHeader);

            final ByteBuffer fillEntry = ByteBuffer.allocate((int)txnEntryBufferSize);
            while(fillEntry.hasRemaining()) fillEntry.put(FILLER);

            for (int entry = 0; entry < txnEntryBufferCount; ++entry) {
                fillEntry.rewind();
                channel.write(fillEntry);
            }

            if (channel.size() != (headerSize + totalEntrySize)) {
                // This should only happen if there's an error in the code.
                throw new IllegalStateException("Channel size mismatch!");
            }

            final MappedByteBuffer headerByteBuffer = channel.map(READ_WRITE, 0, header.size());
            header.setByteBuffer(headerByteBuffer, 0);
            header.magic.set(JOURNAL_MAGIC);
            header.major.set(VERSION_MAJOR_CURRENT);
            header.minor.set(VERSION_MINOR_CURRENT);
            header.txnBufferSize.set(txnEntryBufferSize);
            header.txnBufferCount.set(txnEntryBufferCount);

            return channel.map(READ_WRITE, header.size(), totalEntrySize);

        }

    }

    @Override
    public Entry getCurrentEntry() {
        try {
            rLock.lock();
            final Revision<?> nextRevision = unixFSRevisionPool.nextRevision(current);
            final Entry entry = new UnixFSJournalEntry(nextRevision);
            return entry;
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public MutableEntry newMutableEntry() {
        try {
            rLock.lock();
            final Revision<?> nextRevision = unixFSRevisionPool.nextRevision(current);
            final MutableEntry entry = new UnixFSJournalMutableEntry(nextRevision);
            return entry;
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public Stream<ResourceId> clear() {

        if (!wLock.tryLock()) throw new IllegalStateException("Operation must be performed in exclusive transaction.");

        try {
            // TODO Clear Journal by moving to other location on disk.
            return null;
        } finally {
            wLock.unlock();
        }

    }

    @Override
    public Monitor getExclusiveMonitor() {

        wLock.lock();

        return new Monitor() {

            @Override
            public void close() {
                wLock.unlock();
            }

            @Override
            public Condition getCondition(final String name) {
                throw new UnsupportedOperationException("Conditions are't supported for this implementation.");
            }

        };

    }

    @Override
    public void close() {
        journalBuffer.force();
        utils.unlockDirectory(lockFilePath);
    }

    private class UnixFSJournalEntry implements Entry {

        protected boolean open = true;

        protected final Revision<?> revision;

        protected final LazyValue<SortedMap<Path, ResourceId>> pathMap;

        protected final LazyValue<SetMultimap<ResourceId, Path>> reversePathMap;

        public UnixFSJournalEntry(final Revision revision) {
            rLock.lock();
            this.revision = revision;
            this.pathMap = new LazyValue<>(TreeMap::new);
            this.reversePathMap = new LazyValue<>(HashMultimap::create);
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
                    unixFSGarbageCollector.unlock(revision);
                } finally {
                    rLock.unlock();
                }
            }
        }

        @Override
        public Revision<Boolean> exists(final ResourceId resourceId) {
            check();

            final Optional<Boolean> optionalBoolean =  pathMap
                .getOptional()
                .map(v -> v.containsKey(resourceId));

            return revision.withOptionalValue(optionalBoolean);

        }

        @Override
        public Revision<Stream<ResourceService.Listing>> list(final com.namazustudios.socialengine.rt.Path path) {
            check();

            final Stream<ResourceService.Listing> listingStream;

            if (path.isWildcard()) {
                listingStream = listMultiple(path);
            } else {
                listingStream = listSingular(path);
            }

            return revision.withValue(listingStream);

        }

        private Stream<ResourceService.Listing> listSingular(final com.namazustudios.socialengine.rt.Path path) {
            return pathMap
                .getOptional()
                .flatMap(map -> Optional.ofNullable(map.get(path)))
                .map(resourceId -> new EntryListing(path, resourceId))
                .map(ResourceService.Listing.class::cast)
                .map(Stream::of)
                .orElseGet(Stream::empty);
        }

        private Stream<ResourceService.Listing> listMultiple(final com.namazustudios.socialengine.rt.Path path) {
            final com.namazustudios.socialengine.rt.Path first = path.stripWildcard();
            return pathMap.getOptional().map(map -> stream(
                    new AbstractSpliterator<ResourceService.Listing>(MAX_VALUE, 0) {

                        final Iterator<Map.Entry<Path, ResourceId>> iterator = map
                            .headMap(first)
                            .entrySet()
                            .iterator();

                        @Override
                        public boolean tryAdvance(final Consumer<? super ResourceService.Listing> consumer) {

                            // Check we actually can find the value.
                            if (!iterator.hasNext()) return false;

                            // Check that the current entry matches the original wildcard path
                            final Map.Entry<Path, ResourceId> current = iterator.next();
                            if (!path.matches(current.getKey())) return false;

                            // Finally if both tests pass, then we can make the entry and supply it ot the spliterator
                            consumer.accept(new EntryListing(current));
                            return true;

                        }

                    }, false)
            ).orElseGet(Stream::empty);
        }

        @Override
        public Revision<ResourceId> getResourceId(final Path path) {
            check();
            final Optional<ResourceId> resourceIdOptional = pathMap.getOptional().map(m -> m.get(path));
            return revision.withOptionalValue(resourceIdOptional);
        }

        @Override
        public Revision<ReadableByteChannel> loadResourceContents(final Path path) throws IOException {
            check();
            return revision.withOptionalValue(Optional.empty());
        }

        @Override
        public Revision<ReadableByteChannel> loadResourceContents(final ResourceId resourceId) throws IOException {
            check();
            return revision.withOptionalValue(Optional.empty());
        }

        protected void check() {
            if (!open) throw new IllegalStateException();
        }

    }

    private class UnixFSJournalMutableEntry extends UnixFSJournalEntry implements MutableEntry {

        private boolean committed = false;

        private final LazyValue<Map<ResourceId, Path>> temporaryResourceFiles;

        private final ArrayList<Object> toRelease = new ArrayList<>();

        private final UnixFSTransactionProgram.Builder programBuilder = programBuilderProvider.get();

        public UnixFSJournalMutableEntry(final Revision<?> revision) {
            super(revision);
            this.temporaryResourceFiles = new LazyValue<>(HashMap::new);
        }

        @Override
        public WritableByteChannel saveNewResource(
                final com.namazustudios.socialengine.rt.Path path,
                final ResourceId resourceId) throws IOException, TransactionConflictException {

            check();

            lock(path);
            lock(resourceId);

            final java.nio.file.Path temporaryFile = unixFSGarbageCollector.allocateTemporaryFile();
            final FileChannel fileChannel = FileChannel.open(temporaryFile, WRITE);

            return new WritableByteChannel() {
                @Override
                public int write(ByteBuffer byteBuffer) throws IOException {
                    return fileChannel.write(byteBuffer);
                }

                @Override
                public boolean isOpen() {
                    return fileChannel.isOpen();
                }

                @Override
                public void close() throws IOException {
                    fileChannel.close();
                    programBuilder.link(temporaryFile, resourceId)
                                  .link(temporaryFile, path)
                                  .unlink(temporaryFile);
                }

            };

        }

        @Override
        public void linkNewResource(
                final ResourceId sourceResourceId,
                final com.namazustudios.socialengine.rt.Path path) throws TransactionConflictException {
            check();
            lock(path);
            lock(sourceResourceId);
            programBuilder.linkResource(sourceResourceId, path);
        }

        @Override
        public void linkExistingResource(
                final ResourceId sourceResourceId,
                final com.namazustudios.socialengine.rt.Path destination) throws TransactionConflictException {
            check();
            lock(sourceResourceId);
            lock(destination);
            programBuilder.linkResource(sourceResourceId, destination);
        }

        @Override
        public ResourceService.Unlink unlinkPath(final com.namazustudios.socialengine.rt.Path path) throws TransactionConflictException {
            if (path.isWildcard()) throw new IllegalArgumentException("Wildcard paths not supported.");
            check();
            lock(path);
            programBuilder.unlink(path);
            return null;
        }

        @Override
        public List<ResourceService.Unlink> unlinkMultiple(
                final com.namazustudios.socialengine.rt.Path path,
                final int max) {
            check();
            return null;
        }

        @Override
        public void removeResource(final ResourceId resourceId) throws TransactionConflictException {
            check();
            lock(resourceId);
        }

        @Override
        public List<ResourceId> removeResources(
                final com.namazustudios.socialengine.rt.Path path,
                final int max) {
            if (path.isWildcard()) throw new IllegalArgumentException("Wildcard paths not supported.");
            check();
            return null;
        }

        @Override
        public void commit() {
            check();
            committed = true;
        }

        @Override
        public boolean isCommitted() {
            return committed;
        }

        @Override
        protected void check() {
            super.check();
            if (committed) throw new IllegalStateException();
        }

        private void lock(final Object object) throws TransactionConflictException {

            final MutableEntry entry = lockedResources.putIfAbsent(object, this);

            if (entry == this || entry == null) {
                toRelease.add(object);
            } else {
                throw new TransactionConflictException();
            }

        }

    }

    private static class EntryListing implements ResourceService.Listing {

        private final Path path;

        private final ResourceId resourceId;

        public EntryListing(final Path path, final ResourceId resourceId) {
            this.path = path;
            this.resourceId = resourceId;
        }

        public EntryListing(final Map.Entry<Path, ResourceId> current) {
            this(current.getKey(), current.getValue());
        }

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public ResourceId getResourceId() {
            return resourceId;
        }

    }

}
