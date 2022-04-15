package com.namazustudios.socialengine.rt.remote.provider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.ExecutorService;

import static com.namazustudios.socialengine.rt.remote.Instance.THREAD_GROUP;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class CachedThreadPoolProvider implements Provider<ExecutorServiceFactory<ExecutorService>> {

    private Provider<ThreadGroup>threadGroupProvider;

    @Override
    public ExecutorServiceFactory<ExecutorService> get() {
        return name -> {
            final var group = getThreadGroupProvider().get();
            final var factory = new InstanceThreadFactory(group, name);
            return newCachedThreadPool(factory);
        };
    }

    public Provider<ThreadGroup> getThreadGroupProvider() {
        return threadGroupProvider;
    }

    @Inject
    public void setThreadGroupProvider(@Named(THREAD_GROUP) Provider<ThreadGroup> threadGroupProvider) {
        this.threadGroupProvider = threadGroupProvider;
    }

}
