package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.interrupted;

/**
 * Created by patricktwohig on 4/12/17.
 */
public class SimpleResourceLockService implements ResourceLockService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceLockService.class);

    private static final Thread vacuum;

    private static final ReferenceQueue<SharedLock> references;

    private static final Map<Reference<SharedLock>, Runnable> collections;

    static {
        references = new ReferenceQueue<>();
        collections = new ConcurrentHashMap<>();
        vacuum = new Thread(() -> vacuum());
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
                    if (collection != null) collection.run();
                } catch (InterruptedException ex) {
                    logger.info("Interrupted.  Exiting.", ex);
                    break;
                } catch (Exception ex) {
                    logger.error("Caught exception running cleanup routine.", ex);
                    continue;
                }
            }
        } finally {
            logger.info("Vacuum thread exiting.");
        }

    }

    private final ConcurrentMap<ResourceId, Reference<SharedLock>> resourceIdLockMap = new ConcurrentHashMap<>();

    @Override
    public Monitor getMonitor(final ResourceId resourceId) {

        final SharedLock lock = getLockForId(resourceId);

        lock.lock.lock();

        return new Monitor() {

            @Override
            public Condition getCondition(final String name) {
                return lock.conditions.computeIfAbsent(name, k -> lock.lock.newCondition());
            }

            @Override
            public void close() {
                lock.lock.unlock();
            }

        };

    }

    private SharedLock getLockForId(final ResourceId resourceId) {

        SharedLock lock;

        do {

            lock = resourceIdLockMap.computeIfAbsent(resourceId, rid -> {
                final SharedLock l = new SharedLock();
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

    private class SharedLock {

        private final ReentrantLock lock = new ReentrantLock();

        private final Map<String, Condition> conditions = new ConcurrentHashMap<>();

    }

}
