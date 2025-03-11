package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.Paths;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import dev.getelements.elements.rt.util.SimpleReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static dev.getelements.elements.sdk.cluster.path.Path.WILDCARD;
import static dev.getelements.elements.sdk.cluster.path.Paths.iterateIntermediateHierarchy;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;

public class WeakReferenceLockSetService implements LockSetService {

    private static final Logger logger = LoggerFactory.getLogger(WeakReferenceLockSetService.class);

    private static final Thread vacuum;

    private static final AtomicLong orphans = new AtomicLong();

    private static final ReferenceQueue<ReadWriteLock> references;

    private static final Map<Reference<?>, Runnable> collections;

    static {
        references = new ReferenceQueue<>();
        collections = new ConcurrentHashMap<>();
        vacuum = new Thread(WeakReferenceLockSetService::vacuum);
        vacuum.setName(WeakReferenceLockSetService.class.getSimpleName() + " vacuum.");
        vacuum.setDaemon(true);
        vacuum.start();
    }

    private final PathLockMap pathLockMap = new PathLockMap();

    private final LockMap<ResourceId> resourceIdLockMap = new LockMap<>();

    private static void vacuum() {

        logger.info("Starting vacuum thread.");

        try {
            while(!interrupted()) {
                try {

                    final Reference<?> ref = references.remove();
                    final Runnable collection = collections.remove(ref);

                    if (collection != null) {
                        orphans.incrementAndGet();
                        collection.run();
                    }

                } catch (InterruptedException ex) {
                    logger.info("Interrupted.  Exiting.", ex);
                    break;
                } catch (Exception ex) {
                    logger.error("Caught exception running cleanup routine.", ex);
                }
            }
        } finally {
            logger.info("Vacuum thread exiting.");
        }

    }

    @Override
    public void logStatus() {
        resourceIdLockMap.logStatus();
        pathLockMap.logStatus();
    }

    @Override
    public Monitor getPathReadMonitor(final Path path) {
        return pathLockMap.getMonitor(path, ReadWriteLock::readLock);
    }

    @Override
    public Monitor getPathWriteMonitor(final Path path) {
        return pathLockMap.getMonitor(path, ReadWriteLock::writeLock);
    }

    @Override
    public Monitor getResourceIdReadMonitor(final ResourceId resourceId) {
        return resourceIdLockMap.getMonitor(resourceId, ReadWriteLock::readLock);
    }

    @Override
    public Monitor getResourceIdWriteMonitor(final ResourceId resourceId) {
        return resourceIdLockMap.getMonitor(resourceId, ReadWriteLock::writeLock);
    }

    private static class TaggedLock<TaggedT> implements ReadWriteLock {

        private final TaggedT tag;

        private final ReadWriteLock readWriteLock;

        public TaggedLock(final TaggedT tag) {
            this.tag = tag;
            this.readWriteLock = new ReentrantReadWriteLock() {
                @Override
                public String toString() {
                    return format("%s for %s", getClass().getSimpleName(), tag);
                }
            };
        }

        public TaggedT getTag() {
            return tag;
        }

        @Override
        public Lock readLock() {
            return readWriteLock.readLock();
        }

        @Override
        public Lock writeLock() {
            return readWriteLock.writeLock();
        }

    }

    private static class LockMap<KeyT> {

        private static final Logger logger = LoggerFactory.getLogger(LockMap.class);

        private final ConcurrentNavigableMap<KeyT, Reference<TaggedLock<KeyT>>> map;

        public LockMap() {
            map = new ConcurrentSkipListMap<>();
        }

        public LockMap(final Comparator<KeyT> comparator) {
            map = new ConcurrentSkipListMap<>(comparator);
        }

        private Monitor getMonitor(final KeyT key, final Function<ReadWriteLock, Lock> monitorFunction) {

            final var taggedLock = new SimpleReference<TaggedLock<KeyT>>();
            final var lockLazyValue = new SimpleLazyValue<>(() -> new TaggedLock<>(key));

            map.compute(key, (_key, existingReference) -> {

                var existingvalue = taggedLock.set(
                        existingReference == null
                                ? null
                                : existingReference.get()
                );

                if (existingvalue == null) {

                    existingvalue = taggedLock.set(lockLazyValue.get());

                    final var reference = new SoftReference<>(existingvalue, references);
                    collections.put(reference, () -> cleanup(_key));

                    return reference;

                } else {
                    return existingReference;
                }

            });

            final var lock = monitorFunction.apply(taggedLock.get());
            return Monitor.enter(lock);

        }

        private void cleanup(final KeyT key) {
            if (map.remove(key) == null) {
                logger.warn("Attempting to clean up lock which was already cleaned up {}.", key);
            } else {
                logger.debug("Cleaned up orphaned lock {}.", key);
            }
        }

        public void logStatus() {
            for (final var entry : map.entrySet()) {

                final var ref = entry.getValue();
                final var lock = ref.get();

                logger.debug("LockMap: {} -> (R{}/W{})",
                        entry.getKey(),
                        lock == null ? ref : lock.readWriteLock.readLock(),
                        lock == null ? ref : lock.readWriteLock.writeLock()
                );

            }
        }

    }

    private static class PathLockMap {

        private final LockMap<Path> lockMap = new LockMap<>(Paths.WILDCARD_FIRST);

        public Monitor getMonitor(final Path path, final Function<ReadWriteLock, Lock> monitorFunction) {

            // This essentially works by ensuring that wildcards are locked using the preference of the locking
            // function (ie read lock vs write lock). It assumes that in order to fully lock a path, the caller
            // must read-lock all intermediate paths, including the wildcards implicitly and explicitly lock paths
            // based on the caller preference (which may be an additional read lock). This ensures that exclusive
            // write locks are only taken where the caller specifies a wildcard.
            //
            // 1) Implicit Read Lock
            // 2) Caller Preference - Specified by the monitorFunction (either read or write)
            //
            // For example, when locking the following path, the order of acquisition is as follows:
            // R  - Read
            // CP - Caller Preference
            //
            // Path: context://a/b/c
            //  1: R:  *://
            //  2: R:  context://
            //  3: R:  context://*
            //  4: R:  context://a
            //  6: R:  context://a/*
            //  7: R:  context://a/b
            //  8: R:  context://a/b/*
            //  9: R:  context://a/b/c
            // 10: CP: context://a/b/c/*
            //
            // This ensures that whoever wants to operate against the final path has their preference over all wildcards
            // in the stack. For the purposes of this algorithm, the wildcard and recursive wildcard mean the same
            // because it always appears at the end of the path.
            //
            // If the caller were to explicitly request a lock for a wildcard, that is equivalent to simply locking the
            // at that level and therefore no further locking is needed. However, in that case, we must honor the
            // requested lock type.
            //
            // Path: context://a/*/c
            //  1: R:  *://
            //  2: R:  context://
            //  3: R:  context://*
            //  4: R:  context://a
            //  5: CP: context://a/*
            //
            // When specifying the wildcard the only lock will be first lock that everyone has to acquire. This also
            // means that a wildcard context lock is equivalent to locking the whole database.
            //
            // Path: *://a/b/c
            //  1: CP ://
            //
            // The summary is that all intermediate paths as well as the requested path are locked for reading. Then
            // the caller preference is used for a wildcard under the requested path. This ensures that:
            // - A write lock will always the deepest in the hierarchy
            // - All locks will be acquired in the same consistent order, which mitigates deadlocks
            // - When requesting a wildcard explicitly, the caller-preferred lock will stop at that wildcard

            var monitor = Monitor.empty();

            try {

                final var root = path.contextRootPath();

                if (path.isWildcardContext()) {
                    final var wildcard = root.toPathWithContext(WILDCARD);
                    monitor = monitor.then(lockMap.getMonitor(wildcard, monitorFunction));
                    return monitor;
                } else {
                    final var wildcard = root.toPathWithContext(WILDCARD);
                    monitor = monitor.then(lockMap.getMonitor(wildcard, ReadWriteLock::readLock));
                    monitor = monitor.then(lockMap.getMonitor(root, ReadWriteLock::readLock));
                }

                var parent = root;

                for (var intermediate : iterateIntermediateHierarchy(path)) {

                    final var wildcard = parent.appendComponents(WILDCARD);

                    if (intermediate.isWildcardTerminated() || intermediate.isWildcardRecursive()) {
                        monitor = monitor.then(lockMap.getMonitor(intermediate, monitorFunction));
                        return monitor;
                    } else {
                        monitor = monitor.then(lockMap.getMonitor(wildcard, ReadWriteLock::readLock));
                        monitor = monitor.then(lockMap.getMonitor(intermediate, ReadWriteLock::readLock));
                    }

                    parent = intermediate;

                }

                if (!parent.equals(path)) {
                    // This should never happen, but is a paranoid check and an easy place to set a breakpoint
                    // if we aren't iterating paths correctly.
                    throw new InternalException("Expected paths to match but got " + path + " != " + parent);
                }

                final var wildcard = parent.appendComponents(WILDCARD);
                monitor = monitor.then(lockMap.getMonitor(wildcard, monitorFunction));


            } catch (Exception ex) {
                monitor.close();
                throw ex;
            }

            return monitor;

        }

        public void logStatus() {
            lockMap.logStatus();
        }

    }

}
