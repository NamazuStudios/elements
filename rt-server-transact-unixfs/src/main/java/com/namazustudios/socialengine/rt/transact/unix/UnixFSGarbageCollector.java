package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Implements the garbage collection for the {@link SimpleTransactionalResourceServicePersistence}.
 */
public class UnixFSGarbageCollector {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSGarbageCollector.class);

    private UnixFSUtils utils;

    private UnixFSRevisionTable revisionTable;

    private UnixFSRevisionDataStore revisionDataStore;

    private Provider<UnixFSGarbageCollectionCycle> collectionProvider;

    private final AtomicReference<Context> context = new AtomicReference<>();

    public void start() {

        final Context context = new Context();

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    public void stop() {
        final Context context = this.context.getAndSet(null);
        if (context == null) throw new IllegalStateException("Not currently running.");
        context.stop();
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

    public UnixFSRevisionDataStore getRevisionDataStore() {
        return revisionDataStore;
    }

    @Inject
    public void setRevisionDataStore(UnixFSRevisionDataStore revisionDataStore) {
        this.revisionDataStore = revisionDataStore;
    }

    public Provider<UnixFSGarbageCollectionCycle> getCollectionProvider() {
        return collectionProvider;
    }

    @Inject
    public void setCollectionProvider(final Provider<UnixFSGarbageCollectionCycle> collectionProvider) {
        this.collectionProvider = collectionProvider;
    }

    private class Context {

        private final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        public Context() {
            executor.scheduleAtFixedRate(this::collect, 10, 1, SECONDS);
        }

        private void collect() {

            final List<UnixFSRevisionTableEntry> revisions = getRevisionTable().findCollectableRevisions();

            if (revisions.isEmpty()) {
                logger.info("No revisions to collect.");
            } else {
                collect(revisions);
            }

        }

        private void collect(final List<UnixFSRevisionTableEntry> revisions) {
            final UnixFSGarbageCollectionCycle collection = getCollectionProvider().get();
            collection.collect(revisions.get(revisions.size() - 1));
        }

        private void stop() {

            executor.shutdown();

            try {
                executor.awaitTermination(30, SECONDS);
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

    }

}
