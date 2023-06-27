package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.annotation.Proxyable;
import dev.getelements.elements.rt.annotation.RemotelyInvokable;

import javax.inject.Inject;
import javax.inject.Provider;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.getelements.elements.rt.Reflection.methods;

public class RemoteProxyProvider<ProxyableT> implements Provider<ProxyableT> {

    private final String name;

    private final Class<ProxyableT> interfaceClassT;

    private Provider<RemoteInvocationDispatcher> remoteInvocationDispatcherProvider;

    public RemoteProxyProvider(final Class<ProxyableT> proxyableInterface) {
        this(proxyableInterface, null);
    }

    public RemoteProxyProvider(final Class<ProxyableT> interfaceClassT, final String name) {

        if (interfaceClassT.getAnnotation(Proxyable.class) == null) {
            throw new IllegalArgumentException(interfaceClassT.getName() + " is not @Proxyable");
        }

        this.name = name;
        this.interfaceClassT = interfaceClassT;

    }

    @Override
    public ProxyableT get() {

        final RemoteInvocationDispatcher remoteInvocationDispatcher = getRemoteInvocationDispatcherProvider().get();

        final ProxyBuilder<ProxyableT> builder = new ProxyBuilder<>(interfaceClassT, name)
            .withToString()
            .withDefaultHashCodeAndEquals()
            .withSharedMethodHandleCache()
            .withHandlersForRemoteDispatcher(remoteInvocationDispatcher)
            .dontProxyDefaultMethods();

        return builder.build();

    }

    public Provider<RemoteInvocationDispatcher> getRemoteInvocationDispatcherProvider() {
        return remoteInvocationDispatcherProvider;
    }

    @Inject
    public void setRemoteInvocationDispatcherProvider(Provider<RemoteInvocationDispatcher> remoteInvocationDispatcherProvider) {
        this.remoteInvocationDispatcherProvider = remoteInvocationDispatcherProvider;
    }

}
