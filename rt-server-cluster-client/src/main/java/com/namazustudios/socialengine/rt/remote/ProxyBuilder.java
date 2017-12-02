package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

public class ProxyBuilder<ProxyT> {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBuilder.class);

    private ClassLoader classLoader;

    private final Class<ProxyT> interfaceClassT;

    private final Map<Method, InvocationHandler> handlerMap = new HashMap<>();

    /**
     * Creates a {@link ProxyBuilder<ProxyT>} for the supplied interface type
     *
     * @param interfaceClassT
     */
    public ProxyBuilder(final Class<ProxyT> interfaceClassT) {
        this.interfaceClassT = interfaceClassT;
        classLoader = interfaceClassT.getClassLoader();
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
     * Returns new instance of {@link ProxyT} using the built-in {@link java.lang.reflect.Proxy} functionality.
     *
     * @return the {@link ProxyT} instance.
     */
    public ProxyT build() {

        final Map<Method, InvocationHandler> handlerMap = new HashMap<>(this.handlerMap);

        final Set<Method> definedMethodSet = new HashSet<>(handlerMap.keySet());
        final Set<Method> availableMethodSet = methods().collect(Collectors.toSet());

        if (!definedMethodSet.equals(availableMethodSet)) {

            final Set<Method> undefinedMethods = new HashSet<>(availableMethodSet);
            undefinedMethods.removeAll(definedMethodSet);

            final String undefinedMethodList = undefinedMethods
                .stream()
                .map(method -> {
                    final String parameterSpec = stream(method.getParameterTypes()).map(c -> c.getName()).collect(joining(","));
                    return format("%s.%s(%s)", method.getDeclaringClass().getName(), method.getName(), parameterSpec);
                })
                .collect(joining("\n"));

            logger.warn("Some methods undefined for {}:\n{}", undefinedMethodList);

        }

        final InvocationHandler throwingInvocationHandler = (p, method, args) -> {
            throw new NoSuchMethodError("No invocation handler for method: " + method);
        };

        final Object proxy = newProxyInstance(classLoader, new Class<?>[]{interfaceClassT}, (p, method, args) -> {
            final InvocationHandler invocationHandler =  handlerMap.getOrDefault(method, throwingInvocationHandler);
            return invocationHandler.invoke(p, method, args);
        });

        return interfaceClassT.cast(proxy);

    }

    private Stream<Method> methods() {

        Stream<Method> methodStream = empty();

        for (Class<?> cls = interfaceClassT; cls != Object.class; cls = cls.getSuperclass()) {
            methodStream = concat(methodStream, stream(cls.getMethods()));
        }

        return methodStream;
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
            final String msg = format("No such method: %s.%s()", interfaceClassT.getName(), name);
            return new IllegalArgumentException(msg);
        }

        private IllegalArgumentException noSuchMethod(final String name, final Class<?>[] args) {
            final String parameterSpec = stream(args).map(c -> c.getName()).collect(joining(","));
            final String msg = format("No such method: %s.%s(%s)", interfaceClassT.getName(), name, parameterSpec);
            return new IllegalArgumentException(msg);
        }

    }

}
