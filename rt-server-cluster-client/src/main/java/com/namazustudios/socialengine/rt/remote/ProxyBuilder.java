package com.namazustudios.socialengine.rt.remote;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.namazustudios.socialengine.rt.Reflection;
import com.namazustudios.socialengine.rt.annotation.Proxyable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.reflect.Proxy.newProxyInstance;

public class ProxyBuilder<ProxyT> {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBuilder.class);

    private ClassLoader classLoader;

    private InvocationHandler defaultInvocationHandler = (p, method, args) -> {
        throw new NoSuchMethodError("No invocation handler for method: " + method);
    };

    private final Class<ProxyT> interfaceClassT;

    private final Map<Method, InvocationHandler> handlerMap = new HashMap<>();

    /**
     * Creates a {@link ProxyBuilder<ProxyT>} for the supplied interface type
     *
     * @param interfaceClassT
     */
    public ProxyBuilder(final Class<ProxyT> interfaceClassT) {

        if (interfaceClassT.getAnnotation(Proxyable.class) == null) {
            throw new IllegalArgumentException(interfaceClassT.getName() + " is not @Proxyable");
        }

        this.interfaceClassT = interfaceClassT;
        classLoader = interfaceClassT.getClassLoader();

    }

    /**
     * Given any interface methods that are declared as "default" this will ensure that they are not proxied.
     *
     * @return this instance
     */
    public ProxyBuilder<ProxyT> dontProxyDefaultMethods() {
        final LoadingCache<Object, LoadingCache<Method, MethodHandle>> proxyCache = proxyCache();
        final InvocationHandler handler = (p, method, args) -> proxyCache.get(p).get(method).invoke(args);
        methods().filter(m -> m.isDefault()).forEach(m -> handler(handler).forMethod(m));
        return this;
    }

    final LoadingCache<Object, LoadingCache<Method, MethodHandle>> proxyCache() {
        return CacheBuilder.newBuilder().weakKeys().build(proxyLoader());
    }

    private CacheLoader<Object, LoadingCache<Method, MethodHandle>> proxyLoader() {
        return new CacheLoader<Object, LoadingCache<Method, MethodHandle>>() {
            @Override
            public LoadingCache<Method, MethodHandle> load(final Object proxy) throws Exception {
                return CacheBuilder
                    .newBuilder()
                    .weakValues()
                    .build(new CacheLoader<Method, MethodHandle>() {
                        @Override
                        public MethodHandle load(final Method method) throws Exception {
                            return MethodHandles
                                .lookup()
                                .in(interfaceClassT)
                                .unreflectSpecial(method, interfaceClassT)
                                .bindTo(proxy);
                        }
                    });
            }
        };
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
