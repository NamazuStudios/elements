package com.namazustudios.socialengine.rt.remote;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.namazustudios.socialengine.rt.IocResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableList;

public class IoCInvocationDispatcher extends AbstractInvocationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(IoCInvocationDispatcher.class);

    private IocResolver iocResolver;

    @Override
    protected Object resolve(final Class<?> type) {
        return getIocResolver().inject(type);
    }

    @Override
    protected Object resolve(final Class<?> type, final String name) {
        return getIocResolver().inject(type, name);
    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

}
