package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.annotation.Proxyable;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

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
