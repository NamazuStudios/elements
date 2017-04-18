package com.namazustudios.socialengine.rt.worker;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.handler.SimpleHandlerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/23/15.
 */
public class SimpleInternalContainer implements Container<Worker> {

    /**
     * The SimpleHandlerContainer uses an {@link ExecutorService} to process requests and dispatch
     * events to the various {@link Resource}s.  This names the specific {@link ExecutorService}
     * to use for injectiong using {@link Named}
     */
    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.AbstractSimpleContainer.executorService";

    private static final Logger LOG = LoggerFactory.getLogger(SimpleInternalContainer.class);
    private static final Logger logger = LoggerFactory.getLogger(AbstractSimpleContainer.class);

    @Inject
    private InternalRequestDispatcher internalRequestDispatcher;

    private ResourceService<Worker> resourceService;

    private LockService lockService;

    private ExecutorService executorService;

    @Override
    public void shutdown() {
        getResourceService().removeAndCloseAllResources();
    }

    public ResourceService<Worker> getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService<Worker> resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public <T> Future<T> perform(ResourceId resourceId, Function<Worker, T> operation) {
        return getExecutorService().submit(() -> {

            final Worker resource = getResourceService().getResourceWithId(resourceId);
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
    public <T> Future<T> perform(Path path, Function<Worker, T> operation) {
        return getExecutorService().submit(() -> {

            final Worker resource = getResourceService().getResourceAtPath(path);
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
    public void setExecutorService(@Named(SimpleHandlerContainer.EXECUTOR_SERVICE) ExecutorService executorService) {
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
