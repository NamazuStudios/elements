package com.namazustudios.socialengine.rt.remote.provider;

import java.util.concurrent.ExecutorService;

@FunctionalInterface
public interface ExecutorServiceFactory<ExecutorServiceFactoryT extends ExecutorService> {

    ExecutorServiceFactoryT getService(String name);

    default ExecutorServiceFactoryT getService(final Class<?> enclosing) {
        return getService(enclosing.getName());
    }

}
