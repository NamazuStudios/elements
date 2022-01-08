package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Implements the garbage collection for the {@link JournalTransactionalResourceServicePersistenceEnvironment}.
 */
public class UnixFSGarbageCollector {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSGarbageCollector.class);

    private UnixFSUtils utils;

    private UnixFSRevisionTable revisionTable;

    private Provider<UnixFSGarbageCollectionCycle> collectionCycleProvider;

    private final AtomicReference<Context> context = new AtomicReference<>();

    private final AtomicReference<Thread.UncaughtExceptionHandler> uncaughtExceptionHandler = new AtomicReference<>();

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
     * Forces a garbage collection cycle synchronously. This will perform a collection and block until the cycle runs.
     * If a cycle is currently running, or multiple requests are queued, it this will block until the requested cycle
     * has run. If the garbage collector is paused, this will ignore the pause flag and process the cycle anyhow.
     */
    public void forceSync() {

        final Future<Void> future = getContext().forceSync();

        try {
            future.get();
        } catch (InterruptedException ex) {
            throw new InternalException("Interrupted while waiting.", ex);
        } catch (ExecutionException e) {

            final Throwable cause = e.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else {
                throw new InternalException(e);
            }

        }

    }

    /**
     * Hints to the garbage collector that it should run a cycle immediately. If the collector is paused or currently
     * running the collector will ignore the hint.
     */
    public void hintImmediateAsync() {
        getOptionalContext().ifPresent(Context::hintImmediateAsync);
    }

    /**
     * Pauses or un-pauses the garbage collector.
     *
     * @param paused true if paused, false otherwise
     */
    public void setPaused(final boolean paused) {
        getContext().setPaused(paused);
    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    private Optional<Context> getOptionalContext() {
        return Optional.ofNullable(context.get());
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

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler.get();
    }

    public void setUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler.set(uncaughtExceptionHandler);
    }

    private class Context {

        private final ScheduledFuture<?> timedCycle;

        private final AtomicBoolean paused = new AtomicBoolean(false);

        private final AtomicBoolean running = new AtomicBoolean(false);

        private final ScheduledExecutorService executor = newSingleThreadScheduledExecutor(r -> {

            final Thread thread = new Thread(r);

            thread.setDaemon(true);
            thread.setName(UnixFSGarbageCollector.class.getSimpleName() + " Thread");

            thread.setUncaughtExceptionHandler((t, e) -> {
                final Thread.UncaughtExceptionHandler handler = uncaughtExceptionHandler.get();
                logger.error("Error in garbage collection cycle {}", t, e);
                if (handler != null) handler.uncaughtException(t, e);
            });

            return thread;

        });

        public Context() {
            timedCycle = executor.scheduleAtFixedRate(() -> collect(false), 30, 30, SECONDS);
        }

        public Future<Void> forceSync() {
            return executor.submit(() -> {
                this.collect(true);
                return null;
            });
        }

        public void hintImmediateAsync() {
            if (!running.get()) executor.execute(() -> collect(false));
        }

        private void collect(final boolean force) {

            if (!force && paused.get()) {
                logger.info("Garbage Collection paused. Skipping cycle.");
                return;
            }

            running.set(true);

            try (final UnixFSRevisionTable.RevisionMonitor<List<Slice<UnixFSRevisionTableEntry>>> monitor =
                         getRevisionTable().writeLockCollectibleRevisions()) {

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

            } finally {
                running.set(false);
            }

        }

        private void collect(final List<Slice<UnixFSRevisionTableEntry>> revisions) {
            final UnixFSGarbageCollectionCycle collection = getCollectionCycleProvider().get();
            collection.collect(revisions.stream().map(Slice::getValue).collect(toList()));
        }

        private void stop() {

            timedCycle.cancel(false);
            executor.shutdownNow();

            try {
                executor.awaitTermination(5, MINUTES);
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        public void setPaused(final boolean paused) {
            this.paused.set(paused);
        }

    }

}
