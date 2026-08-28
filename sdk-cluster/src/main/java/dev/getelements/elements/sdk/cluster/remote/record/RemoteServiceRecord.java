package dev.getelements.elements.sdk.cluster.remote.record;

import dev.getelements.elements.sdk.cluster.address.RemoteElementServiceAddress;
import dev.getelements.elements.sdk.cluster.remote.annotation.RemotelyInvokable;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

/**
 * The remote address of a service, together with the addresses of all of its {@link RemotelyInvokable} methods.
 *
 * @param address the address of the service
 * @param methods the addresses of the service's remotely-invokable methods
 */
public record RemoteServiceRecord(RemoteElementServiceAddress address, List<RemoteMethodRecord> methods) {

    public RemoteServiceRecord {
        methods = List.copyOf(methods);
    }

    /**
     * Builds a {@link RemoteServiceRecord} by reflecting over the supplied service type for its
     * {@link RemotelyInvokable} methods.
     *
     * @param address the address of the service
     * @param serviceType the service type to reflect over
     * @return the {@link RemoteServiceRecord}
     */
    public static RemoteServiceRecord from(final RemoteElementServiceAddress address,
                                           final Class<?> serviceType) {

        final var methods = RemotelyInvokable.Util
                .getMethodStream(serviceType)
                .map(method -> new RemoteMethodRecord(address.withMethod(
                        method.getName(),
                        of(method.getParameterTypes()).map(Class::getName).toArray(String[]::new)
                )))
                .collect(toList());

        return new RemoteServiceRecord(address, methods);

    }

}
