package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistence;
import com.namazustudios.socialengine.rt.transact.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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

    private Provider<UnixFSGarbageCollection> collectionProvider;

    private final AtomicReference<Context> context = new AtomicReference<>();

    /**
     * Pins the particular file to the supplied {@link Revision<?>}.  This will guarantee that the file pointed to the
     * {@link Path} will be preserved until the supplied {@link Revision<?>} is released.  This only works if the
     * garbage collector has the {@link Revision} locked.
     *
     * @param file the revision file to pin
     * @param revision the {@link Revision<?>}
     * @return the {@link Path} to the pinned file.
     */
    public Path pin(final Path file, final Revision<?> revision) {
        return file;
//        if (isRegularFile(file, NOFOLLOW_LINKS)) {
//            final Path parent = file.getParent();
//            final Path pinned = utils.resolveRevisionFilePath(parent, revision);
//            if (exists(pinned) || pinned.equals(file)) return file;
//            return getUtils().doOperation(() -> createLink(pinned, file), FatalException::new);
//        } else if (isSymbolicLink(file)) {
//
//            final Path parent = file.getParent();
//            final Path pinned = utils.resolveSymlinkPath(parent, revision);
//            if (exists(pinned) || pinned.equals(file)) return file;
//
//            return getUtils().doOperation(() -> {
//                final Path followed = readSymbolicLink(file);
//                return createSymbolicLink(pinned, followed);
//            }, FatalException::new);
//
//        } else {
//            throw new IllegalArgumentException("Neither file nor symbolic link: " + file);
//        }
    }

    /**
     * Hints that the following {@link Revision<?>} may be eligible for garbage collection.
     *
     * @param revision
     */
    public void hint(final UnixFSRevision<?> revision) {
        // TODO Implement
    }

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

    public Provider<UnixFSGarbageCollection> getCollectionProvider() {
        return collectionProvider;
    }

    @Inject
    public void setCollectionProvider(final Provider<UnixFSGarbageCollection> collectionProvider) {
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
            final UnixFSGarbageCollection collection = getCollectionProvider().get();
            collection.collectUpTo(revisions.get(revisions.size() - 1));
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
