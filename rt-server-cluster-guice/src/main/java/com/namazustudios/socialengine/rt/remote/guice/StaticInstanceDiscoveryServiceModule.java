package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.remote.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.remote.InstanceHostInfo;
import com.namazustudios.socialengine.rt.remote.SimpleInstanceHostInfo;
import com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.HOST_INFO;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;

public class StaticInstanceDiscoveryServiceModule extends PrivateModule {

    private Runnable bindAddresses = () -> {};

    public StaticInstanceDiscoveryServiceModule withInstanceAddresses(final List<String> instanceAddresses) {
        return withInstanceAddresses(instanceAddresses.stream().collect(joining(",")));
    }

    public StaticInstanceDiscoveryServiceModule withInstanceAddresses(final String instanceAddresses) {
        bindAddresses = () -> bind(String.class).annotatedWith(named(HOST_INFO)).toInstance(instanceAddresses);
        return this;
    }

    @Override
    protected void configure() {

        bindAddresses.run();

        bind(InstanceDiscoveryService.class).to(StaticInstanceDiscoveryService.class).asEagerSingleton();

        convertToTypes(only(new TypeLiteral<Set<InstanceHostInfo>>(){}), (value, toType) -> {
            final Set<InstanceHostInfo> hosts = new HashSet<>();
            for (final String addr : value.split("[\\s,]+")) hosts.add(new SimpleInstanceHostInfo(addr));
            return unmodifiableSet(hosts);
        });

        expose(InstanceDiscoveryService.class);

    }

}
