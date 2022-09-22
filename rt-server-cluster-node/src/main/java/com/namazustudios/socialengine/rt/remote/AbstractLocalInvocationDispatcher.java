package com.namazustudios.socialengine.rt.remote;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import com.namazustudios.socialengine.rt.exception.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.Constants.REMOTE_PROTOCOL;
import static com.namazustudios.socialengine.rt.Constants.REMOTE_SCOPE;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

public abstract class AbstractLocalInvocationDispatcher implements LocalInvocationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLocalInvocationDispatcher.class);

    private String scope;

    private String protocol;

    private final LoadingCache<MethodKey, LocalInvocationProcessor> localDispatcherCache = CacheBuilder
        .newBuilder()
        .weakKeys()
        .build(new CacheLoader<>() {

            @Override
            public LocalInvocationProcessor load(final MethodKey key) throws Exception {

                final var definition = Stream
                      .of(key.getType().getAnnotationsByType(RemoteService.class))
                      .flatMap(rs -> Stream.of(rs.value()))
                      .filter(d -> getScope().equals(d.scope()))
                      .filter(d -> getProtocol().equals(d.protocol()))
                      .findFirst()
                      .orElseThrow(() -> new ServiceNotFoundException(format(
                          "Service Not found for %s (%s - %s)",
                          key.getType().getName(),
                          getScope(),
                          getProtocol()
                      )));

                return new LocalInvocationProcessorBuilder(
                    definition,
                    key.getType(),
                    key.getMethod(),
                    key.getParameters()).build();

            }
        });

    @Override
    public void dispatch(final Invocation invocation,
                         final Consumer<InvocationResult> syncInvocationResultConsumer,
                         final Consumer<InvocationError> syncInvocationErrorConsumer,
                         final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList,
                         final Consumer<InvocationError> asyncInvocationErrorConsumer) {

        try {

            final Class<?> type = Class.forName(invocation.getType());
            final Object object = resolve(type, invocation);

            doDispatch(
                type, object, invocation,
                syncInvocationResultConsumer, syncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        } catch (Exception ex) {
            logger.error("Caught exception resolving target for invocation.", ex);
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(ex);
            syncInvocationErrorConsumer.accept(invocationError);
            asyncInvocationErrorConsumer.accept(invocationError);
        }

    }

    private void doDispatch(
            final Class<?> type,
            final Object object,
            final Invocation invocation,
            final Consumer<InvocationResult> syncInvocationResultConsumer,
            final Consumer<InvocationError> syncInvocationErrorConsumer,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final Consumer<InvocationError> asyncInvocationErrorConsumer) throws Exception {

        final LocalInvocationProcessor localInvocationDispatcher;

        try {
            final MethodKey methodKey = new MethodKey(type, invocation);
            localInvocationDispatcher = localDispatcherCache.get(methodKey);
        } catch (ExecutionException ex) {
            logger.error("Caught exception resolving target for invocation.", ex);
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(ex);
            asyncInvocationErrorConsumer.accept(invocationError);
            return;
        }

        localInvocationDispatcher.processInvocation(
                object, invocation,
                syncInvocationResultConsumer, syncInvocationErrorConsumer,
                asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);

    }

    protected Object resolve(final Class<?> type, final Invocation invocation) {
        final String name = invocation.getName();
        return name == null ? resolve(type) : resolve(type, name);
    }

    protected Object resolve(Class<?> type) {
        throw new InternalError("No target for " + type.getName());
    }

    protected Object resolve(Class<?> type, String name) {
        throw new InternalError("No target for " + type.getName() + " with name " + name);
    }

    public String getScope() {
        return scope;
    }

    @Inject
    public void setScope(@Named(REMOTE_SCOPE) String scope) {
        this.scope = scope;
    }

    public String getProtocol() {
        return protocol;
    }

    @Inject
    public void setProtocol(@Named(REMOTE_PROTOCOL) String protocol) {
        this.protocol = protocol;
    }

    private static class MethodKey {

        private final Class<?> type;

        private final String method;

        private final List<String> parameters;

        public MethodKey(final Class<?> type, final Invocation invocation) {
            this(type, invocation.getMethod(), invocation.getParameters());
        }

        public MethodKey(final Class<?> type,
                         final String name,
                         final List<String> parameters) {
            this.type = type;
            this.method = name;
            this.parameters = unmodifiableList(new CopyOnWriteArrayList<>(parameters));
        }

        public Class<?> getType() {
            return type;
        }

        public String getMethod() {
            return method;
        }

        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodKey)) return false;

            MethodKey methodKey = (MethodKey) o;

            if (!getType().equals(methodKey.getType())) return false;
            if (!getMethod().equals(methodKey.getMethod())) return false;
            return getParameters().equals(methodKey.getParameters());
        }

        @Override
        public int hashCode() {
            int result = getType().hashCode();
            result = 31 * result + getMethod().hashCode();
            result = 31 * result + getParameters().hashCode();
            return result;
        }

    }
}
