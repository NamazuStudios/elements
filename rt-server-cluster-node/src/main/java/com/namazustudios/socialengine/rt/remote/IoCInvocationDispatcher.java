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

public class IoCInvocationDispatcher implements InvocationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(IoCInvocationDispatcher.class);

    private IocResolver iocResolver;

    private final LoadingCache<MethodKey, LocalInvocationDispatcher> localDispatcherCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<MethodKey, LocalInvocationDispatcher>() {
            @Override
            public LocalInvocationDispatcher load(final MethodKey key) throws Exception {

                return null;
            }
        });

    @Override
    public void dispatch(final Invocation invocation, final Consumer<InvocationResult> invocationResultConsumer) {
        try {
            final String name = invocation.getName();
            final Class<?> type = Class.forName(invocation.getType());
            final Object object = name == null ? getIocResolver().inject(type) : getIocResolver().inject(type, name);
            doDispatch(type, object, invocation, invocationResultConsumer);
        } catch (Throwable th) {
            logger.error("Caught exception resolving target for invocation.", th);
            final InvocationResult invocationResult = new InvocationResult();
            invocationResult.setOk(false);
            invocationResult.setThrowable(th);
            invocationResultConsumer.accept(invocationResult);
        }
    }

    private void doDispatch(
            final Class<?> type, final Object object,
            final Invocation invocation, final Consumer<InvocationResult> invocationResultConsumer) throws Throwable {
        try {
            final MethodKey methodKey = new MethodKey(type, invocation);
            final LocalInvocationDispatcher localInvocationDispatcher = localDispatcherCache.get(methodKey);
            localInvocationDispatcher.dispatch(object, invocation, invocationResultConsumer);
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

    private static class MethodKey {

        private final Class<?> type;

        private final String name;

        private final List<String> parameters;

        public MethodKey(final Class<?> type, final Invocation invocation) throws ClassNotFoundException {
            this(type, invocation.getName(), invocation.getParameters());
        }

        public MethodKey(final Class<?> type,
                         final String name,
                         final List<String> parameters) throws ClassNotFoundException {
            this.type = type;
            this.name = name;
            this.parameters = unmodifiableList(new CopyOnWriteArrayList<>(parameters));
        }

        public Class<?> getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodKey)) return false;

            MethodKey methodKey = (MethodKey) o;

            if (!type.equals(methodKey.type)) return false;
            if (!name.equals(methodKey.name)) return false;
            return parameters.equals(methodKey.parameters);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + parameters.hashCode();
            return result;
        }

    }

}
