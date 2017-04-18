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
public class SimpleHandlerContainer implements Container<Handler> {

    /**
     * The SimpleHandlerContainer uses an {@link ExecutorService} to process requests and dispatch
     * events to the various {@link Resource}s.  This names the specific {@link ExecutorService}
     * to use for injectiong using {@link Named}
     */
    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.AbstractSimpleContainer.executorService";
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleHandlerContainer.class);

    private ResourceService<Handler> resourceService;
    private LockService lockService;
    private ExecutorService executorService;

    @Override
    public void shutdown() {}

    public ResourceService<Handler> getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService<Handler> resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public <T> Future<T> perform(ResourceId resourceId, Function<Handler, T> operation) {
        return getExecutorService().submit(() -> {

            final Handler resource = getResourceService().getResourceWithId(resourceId);
            final Lock lock = getLockService().getLock(resource.getId());

            try {
                lock.lock();
                return operation.apply(resource);
            } finally {
                lock.unlock();
            }

        });
    }

    @Override
    public <T> Future<T> perform(Path path, Function<Handler, T> operation) {
        return getExecutorService().submit(() -> {

            final Handler resource = getResourceService().getResourceAtPath(path);
            final Lock lock = getLockService().getLock(resource.getId());

            try {
                lock.lock();
                return operation.apply(resource);
            } finally {
                lock.unlock();
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
}
