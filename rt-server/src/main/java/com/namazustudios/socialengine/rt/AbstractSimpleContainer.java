package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/23/15.
 */
public abstract class AbstractSimpleContainer<ResourceT extends Resource> implements Container<ResourceT> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSimpleContainer.class);

    /**
     * The SimpleHandlerContainer uses an {@link ExecutorService} to process requests and dispatch
     * events to the various {@link Resource}s.  This names the specific {@link ExecutorService}
     * to use for injectiong using {@link Named}
     */
    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.AbstractSimpleContainer.executorService";

    private LockService lockService;

    private ExecutorService executorService;

    @Override
    public <T> Future<T> perform(ResourceId resourceId, Function<ResourceT, T> operation) {
        return getExecutorService().submit(() -> {

            final ResourceT resource = getResourceService().getResourceWithId(resourceId);
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
    public <T> Future<T> perform(Path path, Function<ResourceT, T> operation) {
        return getExecutorService().submit(() -> {

            final ResourceT resource = getResourceService().getResourceAtPath(path);
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

    protected abstract ResourceService<ResourceT> getResourceService();


    public LockService getLockService() {
        return lockService;
    }

    @Inject
    public void setLockService(@Named LockService lockService) {
        this.lockService = lockService;
    }

}
