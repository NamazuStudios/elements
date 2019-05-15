package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import javax.inject.Inject;
import javax.inject.Provider;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.Reflection.methods;

public class RemoteProxyProvider<ProxyableT> implements Provider<ProxyableT> {

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private final String name;

    private final Class<ProxyableT> interfaceClassT;

    private RemoteInvokerRegistry remoteInvokerRegistry;

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

        final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();

        final ProxyBuilder<ProxyableT> builder = new ProxyBuilder<>(interfaceClassT)
            .withToString()
            .withDefaultHashCodeAndEquals()
            .withSharedMethodHandleCache()
            .dontProxyDefaultMethods();

        methods(interfaceClassT)
            .filter(m -> m.getAnnotation(RemotelyInvokable.class) != null)
            .map(m -> new RemoteInvocationHandlerBuilder(remoteInvoker, interfaceClassT, m).withName(name))
            .forEach(b -> builder.handler(b.build()).forMethod(b.getMethod()));

        return builder.build();

    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }
}
