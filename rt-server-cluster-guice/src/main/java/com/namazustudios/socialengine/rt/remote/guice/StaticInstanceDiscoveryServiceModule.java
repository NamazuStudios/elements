package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.remote.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.remote.InstanceHostInfo;
import com.namazustudios.socialengine.rt.remote.SimpleInstanceHostInfo;
import com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService;

import java.util.HashSet;
import java.util.Set;

import static com.google.inject.matcher.Matchers.only;
import static java.util.Collections.unmodifiableSet;

public class StaticInstanceDiscoveryServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(InstanceDiscoveryService.class).to(StaticInstanceDiscoveryService.class).asEagerSingleton();

        convertToTypes(only(new TypeLiteral<Set<InstanceHostInfo>>(){}), (value, toType) -> {
            final Set<InstanceHostInfo> hosts = new HashSet<>();
            for (final String addr : value.split("[\\s,]+")) hosts.add(new SimpleInstanceHostInfo(addr));
            return unmodifiableSet(hosts);
        });

        expose(InstanceDiscoveryService.class);

    }

}
