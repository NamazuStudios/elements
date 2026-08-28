package dev.getelements.elements.sdk.cluster.remote.record;

import dev.getelements.elements.sdk.cluster.address.RemoteElementAddress;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceKey;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A snapshot of the full remotely-invokable surface of an {@link ElementRecord}: its address, the services it
 * exposes, and the remotely-invokable methods on each of those services.
 *
 * @param address the address of the element
 * @param services the services exposed by the element
 */
public record RemoteElementRecord(RemoteElementAddress address, List<RemoteServiceRecord> services) {

    public RemoteElementRecord {
        services = List.copyOf(services);
    }

    /**
     * Builds a {@link RemoteElementRecord} from the supplied {@link RemoteElementAddress} and the {@link ElementRecord}
     * describing the element hosted at that address.
     *
     * @param address the address of the element
     * @param elementRecord the {@link ElementRecord} describing the element
     * @return the {@link RemoteElementRecord}
     */
    public static RemoteElementRecord from(final RemoteElementAddress address,
                                           final ElementRecord elementRecord) {

        final var services = elementRecord
                .services()
                .stream()
                .flatMap(ElementServiceKey::from)
                .map(key -> {
                    final var serviceAddress = key.isNamed()
                            ? address.withService(key.type().getName(), key.name())
                            : address.withService(key.type().getName());
                    return RemoteServiceRecord.from(serviceAddress, key.type());
                })
                .collect(toList());

        return new RemoteElementRecord(address, services);

    }

}
