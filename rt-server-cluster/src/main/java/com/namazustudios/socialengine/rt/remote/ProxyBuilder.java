package com.namazustudios.socialengine.rt.remote;

import com.google.common.cache.Cache;
import com.namazustudios.socialengine.rt.Reflection;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.System.identityHashCode;
import static java.lang.reflect.Proxy.newProxyInstance;

public class ProxyBuilder<ProxyT> {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBuilder.class);

    private ClassLoader classLoader;

    private BiFunction<MethodHandleKey, Supplier<MethodHandle>, MethodHandle> methodHandleCache =
        (methodHandleKey, methodHandleSupplier) -> methodHandleSupplier.get();

    private InvocationHandler defaultInvocationHandler = (p, method, args) -> {
        throw new NoSuchMethodError("No invocation handler for method: " + method);
    };

    private final String name;

    private final Class<ProxyT> interfaceClassT;

    private final Map<Method, InvocationHandler> handlerMap = new HashMap<>();

    /**
     * Creates a {@link ProxyBuilder<ProxyT>} for the supplied interface type.
     *
     * @param interfaceClassT
     */
    public ProxyBuilder(final Class<ProxyT> interfaceClassT) {
        this(interfaceClassT, null);
    }

    /**
     * Creates a {@link ProxyBuilder<ProxyT>} for the supplied interface type.
     *
     * @param interfaceClassT
     * @param name Relates to {@link Invocation#getName()} and maps to the naming used in {@link javax.inject.Named}.
     */
    public ProxyBuilder(final Class<ProxyT> interfaceClassT, final String name) {
        this.name = name;
        this.interfaceClassT = interfaceClassT;
        classLoader = interfaceClassT.getClassLoader();
    }

    /**
     * Given any interface methods that are declared as "default" this will ensure that they are not proxied.
     *
     * @return this instance
     */
    public ProxyBuilder<ProxyT> dontProxyDefaultMethods() {

        final InvocationHandler handler = (p, method, args) -> {

            final MethodHandleKey methodHandleKey = new MethodHandleKey(interfaceClassT, p, method);

            final Supplier<MethodHandle> methodHandleSupplier = () -> {
                try {

                    final Class<?> clazz = methodHandleKey.getInterfaceClassT();
                    final MethodType methodType = methodHandleKey.getMethodType();

                    return MethodHandles.lookup()
                        .findSpecial(clazz, method.getName(), methodType, clazz)
                        .bindTo(methodHandleKey.getProxy());

                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new InternalException(e);
                }
            };

            return methodHandleCache.apply(methodHandleKey, methodHandleSupplier).invokeWithArguments(args);

        };

        methods().filter(m -> m.isDefault()).forEach(m -> handler(handler).forMethod(m));
        return this;

    }

    /**
     * Uses the {@link SharedMethodHandleCache#getSharedMethodHandleCache()} to cache method handles.
     *
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withSharedMethodHandleCache() {
        return withMethodHandleCache(((methodHandleKey, methodHandleSupplier) -> {
            final Cache<MethodHandleKey, MethodHandle> cache = SharedMethodHandleCache.getSharedMethodHandleCache();
            try {
                return cache.get(methodHandleKey, () -> methodHandleSupplier.get());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new InternalException(e);
                }
            }
        }));
    }

    /**
     * Allows for hte specification of a cache-getter function.  If the underlying cache
     *
     * @param methodHandleCache a {@link Function<MethodHandleKey, MethodHandle>} used to retrieve cached instances
     * @return
     */
    public ProxyBuilder<ProxyT> withMethodHandleCache(final BiFunction<MethodHandleKey, Supplier<MethodHandle>, MethodHandle> methodHandleCache) {

        if (methodHandleCache == null) {
            throw new IllegalArgumentException("Must specify method handle cache.");
        }

        this.methodHandleCache = methodHandleCache;
        return this;

    }

    /**
     * Specifies an instance of {@link InvocationHandler}, which can be used to handle invocations against a
     * {@link Method} through a {@link MethodAssignment}.
     *
     * @param invocationHandler the {@link InvocationHandler}
     * @return a {@link MethodAssignment} used to assign this {@link InvocationHandler} to a specific {@link Method}
     */
    public MethodAssignment<ProxyBuilder<ProxyT>> handler(final InvocationHandler invocationHandler) {
        return new InvocationHandlerMethodAssignment(invocationHandler);
    }

    /**
     * Specifies the default {@link InvocationHandler}, which gets called when no other {@link InvocationHandler} is
     * able to handle the invocation.
     *
     * @param defaultInvocationHandler the default {@link InvocationHandler}
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withDefaultHandler(final InvocationHandler defaultInvocationHandler) {
        this.defaultInvocationHandler = defaultInvocationHandler;
        return this;
    }

    /**
     * Specifies the a default {@link #toString()} method, which simply returns the value "Proxy for the.class.Name"
     *
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withToString() {
        return withToString("Proxy for " + interfaceClassT.getName());
    }

    /**
     * Specifies the a default {@link #toString()} method, which simply returns the hardcoded value.
     *
     * @param toString the value to return when {@link #toString()} is invoked on the proxy.
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withToString(final String toString) {
        handler((proxy, method, args) -> toString).forMethod("toString");
        return this;
    }

    /**
     * Specifies the {@link #hashCode()} and {@link #equals(Object)} method.  {@link #hashCode()} will be implemented
     * {@link System#identityHashCode(Object)} and equals will be implemented as "=="
     *
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withDefaultHashCodeAndEquals() {
        handler((proxy, method, args) -> identityHashCode(proxy)).forMethod("hashCode");
        handler((proxy, method, args) -> proxy == args[0]).forMethod("equals", Object.class);
        return this;
    }

    /**
     * Generates an {@link InvocationHandler} for each method in the class marked {@link RemotelyInvokable} using the
     * specified {@link RemoteInvoker}.
     *
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withHandlersForRemoteInvoker(final RemoteInvoker remoteInvoker) {
        Reflection.methods(interfaceClassT)
            .filter(m -> m.getAnnotation(RemotelyInvokable.class) != null)
            .map(m -> new RemoteInvocationHandlerBuilder(remoteInvoker, interfaceClassT, m).withName(name))
            .forEach(b -> handler(b.build()).forMethod(b.getMethod()));
        return this;
    }

    /**
     * Generates an {@link InvocationHandler} for each method int he class marked {@link RemotelyInvokable} using the
     * specifed {@link RemoteInvocationDispatcher}.
     *
     * @param remoteInvocationDispatcher the {@link RemoteInvocationDispatcher}
     * @return this instance
     */
    public ProxyBuilder<ProxyT> withHandlersForRemoteDispatcher(final RemoteInvocationDispatcher remoteInvocationDispatcher) {
        Reflection.methods(interfaceClassT)
            .filter(m -> m.getAnnotation(RemotelyInvokable.class) != null)
            .map(m -> new RemoteInvocationHandlerBuilder(remoteInvocationDispatcher, interfaceClassT, m).withName(name))
            .forEach(b -> handler(b.build()).forMethod(b.getMethod()));
        return this;
    }

    /**
     * Returns new instance of {@link ProxyT} using the built-in {@link java.lang.reflect.Proxy} functionality.
     *
     * @return the {@link ProxyT} instance.
     */
    public ProxyT build() {

        final Map<Method, InvocationHandler> handlerMap = new HashMap<>(this.handlerMap);
        final InvocationHandler defaultInvocationHandler = this.defaultInvocationHandler;

        final Object proxy = handlerMap.isEmpty() ?
            newProxyInstance(classLoader, new Class<?>[]{interfaceClassT}, defaultInvocationHandler) :
            newProxyInstance(classLoader, new Class<?>[]{interfaceClassT}, (p, method, args) -> {
                final InvocationHandler invocationHandler =  handlerMap.getOrDefault(method, defaultInvocationHandler);
                return invocationHandler.invoke(p, method, args);
            });

        return interfaceClassT.cast(proxy);

    }

    private Stream<Method> methods() {
        return Reflection.methods(interfaceClassT);
    }

    private class InvocationHandlerMethodAssignment implements MethodAssignment<ProxyBuilder<ProxyT>> {

        private final InvocationHandler invocationHandler;

        public InvocationHandlerMethodAssignment(InvocationHandler invocationHandler) {
            this.invocationHandler = invocationHandler;
        }

        @Override
        public ProxyBuilder<ProxyT> forMethod(final String name) {

            final Method method = methods()
                .filter(m -> m.getName().equals(name) && m.getParameterCount() == 0)
                .findFirst()
                .orElseThrow(() -> noSuchMethod(name));

            forMethod(method);

            return ProxyBuilder.this;

        }

        @Override
        public ProxyBuilder<ProxyT> forMethod(final String name, final Class<?>... args) {

            final Method method = methods()
                .filter(m -> m.getName().equals(name) && Arrays.equals(m.getParameterTypes(), args))
                .findFirst()
                .orElseThrow(() -> noSuchMethod(name, args));

            forMethod(method);

            return ProxyBuilder.this;
        }

        @Override
        public ProxyBuilder<ProxyT> forMethod(final Method method) {

            if (handlerMap.put(method, invocationHandler) != null) {
                logger.warn("Replacing InvocationHandler for method {}", method);
            }

            return ProxyBuilder.this;

        }

        private IllegalArgumentException noSuchMethod(final String name) {
            return Reflection.noSuchMethod(interfaceClassT, name);
        }

        private IllegalArgumentException noSuchMethod(final String name, final Class<?>[] args) {
            return Reflection.noSuchMethod(interfaceClassT, name, args);
        }

    }

}
