package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.interrupted;

/**
 * Created by patricktwohig on 4/12/17.
 */
public class SimpleResourceLockService implements ResourceLockService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceLockService.class);

    private static final Thread vacuum;

    private static final AtomicLong orphans = new AtomicLong();

    private static final ReferenceQueue<SharedLock> references;

    private static final Map<Reference<SharedLock>, Runnable> collections;

    static {
        references = new ReferenceQueue<>();
        collections = new ConcurrentHashMap<>();
        vacuum = new Thread(SimpleResourceLockService::vacuum);
        vacuum.setName(SimpleResourceLockService.class.getSimpleName() + " vacuum.");
        vacuum.setDaemon(true);
        vacuum.start();
    }

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

    private final ConcurrentMap<ResourceId, Reference<SharedLock>> resourceIdLockMap = new ConcurrentHashMap<>();

    /**
     * Returns the number of orphan locks across all {@link SimpleResourceLockService} instances.
     *
     * @return the orphan count
     */
    public static long getOrphanCount() {
        return orphans.get();
    }

    @Override
    public int size() {
        return resourceIdLockMap.size();
    }

    @Override
    public SharedLock getLock(final ResourceId resourceId) {

        SharedLock lock;

        do {

            lock = resourceIdLockMap.computeIfAbsent(resourceId, rid -> {
                final SharedLock l = new SimpleSharedLock();
                final Reference<SharedLock> ref = new SoftReference<>(l, references);
                collections.put(ref, () -> cleanup(rid));
                return ref;
            }).get();

            if (lock == null) delete(resourceId);

        } while (lock == null);

        return lock;

    }

    private void cleanup(final ResourceId resourceId) {
        if (resourceIdLockMap.remove(resourceId) == null) {
            logger.warn("Attempting to clean up lock which was already cleaned up {}.", resourceId);
        } else {
            logger.warn("Cleaned up orphaned lock {}.", resourceId);
        }
    }

    @Override
    public void delete(final ResourceId resourceId) {

        final Reference<SharedLock> ref = resourceIdLockMap.remove(resourceId);

        if (ref == null) {
            logger.warn("Removing lock that was already cleaned up {}", resourceId);
        } else {
            collections.remove(ref);
        }

    }

    private static class SimpleSharedLock implements SharedLock {

        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public Monitor acquireMonitor() {
            lock.lock();
            return lock::unlock;
        }

        @Override
        public Lock getLock() {
            return lock;
        }

    }

}
