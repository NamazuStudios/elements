package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistence;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Implements the garbage collection for the {@link SimpleTransactionalResourceServicePersistence}.
 */
public class UnixFSGarbageCollector {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSGarbageCollector.class);

    private UnixFSUtils utils;

    private UnixFSRevisionTable revisionTable;

    private Provider<UnixFSGarbageCollectionCycle> collectionCycleProvider;

    private final AtomicReference<Context> context = new AtomicReference<>();

    /**
     * Starts this garbage collector.
     */
    public void start() {

        final Context context = new Context();

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    /**
     * Stops this garbage collector.
     */
    public void stop() {
        final Context context = this.context.getAndSet(null);
        if (context == null) throw new IllegalStateException("Not currently running.");
        context.stop();
    }

    /**
     * Hints to the garbage collector that it should run a cycle immediately.
     */
    public void hintImmediate() {
        getContext().hintImmediate();
    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(final UnixFSUtils utils) {
        this.utils = utils;
    }

    public UnixFSRevisionTable getRevisionTable() {
        return revisionTable;
    }

    @Inject
    public void setRevisionTable(UnixFSRevisionTable revisionTable) {
        this.revisionTable = revisionTable;
    }

    public Provider<UnixFSGarbageCollectionCycle> getCollectionCycleProvider() {
        return collectionCycleProvider;
    }

    @Inject
    public void setCollectionCycleProvider(final Provider<UnixFSGarbageCollectionCycle> collectionCycleProvider) {
        this.collectionCycleProvider = collectionCycleProvider;
    }

    private class Context {

        private final AtomicBoolean paused = new AtomicBoolean();

        private final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        public Context() {
            executor.scheduleAtFixedRate(this::collect, 10, 30, SECONDS);
        }

        public void hintImmediate() {
            executor.execute(this::collect);
        }

        private void collect() {

            if (paused.get()) {
                logger.info("Garbage Collection paused. Skipping cycle.");
                return;
            }

            try (final UnixFSRevisionTable.RevisionMonitor<List<Slice<UnixFSRevisionTableEntry>>> monitor = getRevisionTable().writeLockCollectibleRevisions()) {

                final List<Slice<UnixFSRevisionTableEntry>> revisions = monitor.getScope();

                if (revisions.isEmpty()) {
                    logger.info("No revisions to collect. Skipping this GC Cycle.");
                } else {
                    collect(revisions);
                }

                // After all revisions are collected, this clears out the slices such that they may be reclaimed by
                // revision table.

                monitor.getScope().forEach(Slice::clear);
                getRevisionTable().reclaimInvalidEntries();

            }

        }

        private void collect(final List<Slice<UnixFSRevisionTableEntry>> revisions) {
            final UnixFSGarbageCollectionCycle collection = getCollectionCycleProvider().get();
            collection.collect(revisions.stream().map(Slice::getValue).collect(toList()));
        }

        private void stop() {

            executor.shutdown();

            try {
                executor.awaitTermination(5, MINUTES);
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

    }

}
