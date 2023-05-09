package dev.getelements.elements.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.remote.InstanceDiscoveryService;
import dev.getelements.elements.rt.remote.InstanceHostInfo;
import dev.getelements.elements.rt.remote.SimpleInstanceHostInfo;
import dev.getelements.elements.rt.remote.StaticInstanceDiscoveryService;
import dev.getelements.elements.rt.util.HostList;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static java.util.stream.Collectors.toSet;

public class StaticInstanceDiscoveryServiceModule extends PrivateModule {

    private Runnable bindAddresses = () -> {};

    public StaticInstanceDiscoveryServiceModule withInstanceAddresses(final String ... instanceAddresses) {
        return withInstanceAddresses(String.join(",", instanceAddresses));
    }

    public StaticInstanceDiscoveryServiceModule withInstanceAddresses(final List<String> instanceAddresses) {
        return withInstanceAddresses(String.join(",", instanceAddresses));
    }

    public StaticInstanceDiscoveryServiceModule withInstanceAddresses(final String instanceAddresses) {
        bindAddresses = () -> bind(String.class).annotatedWith(named(STATIC_HOST_INFO)).toInstance(instanceAddresses);
        return this;
    }

    @Override
    protected void configure() {

        bindAddresses.run();

        bind(InstanceDiscoveryService.class).to(StaticInstanceDiscoveryService.class).asEagerSingleton();

        convertToTypes(only(new TypeLiteral<Set<InstanceHostInfo>>(){}), (value, toType) -> new HostList()
            .with(value)
            .get()
            .orElseGet(Collections::emptyList)
            .stream()
            .map(SimpleInstanceHostInfo::new)
            .collect(toSet())
        );

        expose(InstanceDiscoveryService.class);

    }

}
