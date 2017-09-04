package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

/**
 * The simple handler server is responsible for dispatching requests and events to all {@link Resource} instances
 * contained therein.  It accomplishes its task in parallel by dispatching all requests, events, and then
 * finally updating each {@link Resource} in order.
 *
 * Internally, it leverages an instance an {@link ExecutorService} and a {@link CompletionService} to
 * perform all updates in parallel.
 *
 * Created by patricktwohig on 8/22/15.
 */
public class SimpleContainer implements Container {

    private static final Logger logger = LoggerFactory.getLogger(SimpleContainer.class);

    /**
     * The SimpleContainer uses an {@link ExecutorService} to process requests and dispatch
     * events to the various {@link Resource}s.  This names the specific {@link ExecutorService}
     * to use for injectiong using {@link Named}
     */
    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.handler.SimpleContainer.executorService";

    private LockService lockService;

    private ExecutorService executorService;

    private ResourceService resourceService;

    @Override
    public void shutdown() {}

    @Override
    public <T> Future<T> perform(final ResourceId resourceId, final Function<Resource, T> operation) {
        return getExecutorService().submit(() -> {

            final Resource resource = getResourceService().getResourceWithId(resourceId);
            final Lock lock = getLockService().getLock(resource.getId());

            try {
                logger.trace("Locking resource {}", resource.getId());
                lock.lock();
                logger.trace("Applying operation for resource {}", resource.getId());
                return operation.apply(resource);
            } finally {
                logger.trace("Unlocking resource {}", resource.getId());
                lock.unlock();
                logger.trace("Unlocked resource {}", resource.getId());
            }

        });
    }

    @Override
    public <T> Future<T> perform(final Path path, final Function<Resource, T> operation) {
        return getExecutorService().submit(() -> {

            final Resource resource = getResourceService().getResourceAtPath(path);
            final Lock lock = getLockService().getLock(resource.getId());

            try {
                logger.trace("Locking resource ({}): {}", path, resource.getId());
                lock.lock();
                logger.trace("Applying operation for resource ({}): {}", path, resource.getId());
                return operation.apply(resource);
            } finally {
                logger.trace("Unlocking resource ({}): {}", path, resource.getId());
                lock.unlock();
                logger.trace("Unlocked resource ({}): {}", path, resource.getId());
            }

        });
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Inject
    public void setExecutorService(@Named(EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = executorService;
    }

    public LockService getLockService() {
        return lockService;
    }

    @Inject
    public void setLockService(@Named LockService lockService) {
        this.lockService = lockService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
