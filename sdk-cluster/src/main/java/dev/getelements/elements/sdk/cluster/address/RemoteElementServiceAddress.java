package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementMethodAddress;
import dev.getelements.elements.sdk.address.ElementServiceAddress;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

import java.util.List;

/**
 * Represnets an element service address
 * @param element the element
 * @param service the service
 */
public record RemoteElementServiceAddress(
        RemoteElementAddress element,
        ElementServiceAddress service) implements AsPath {

    @Override
    public Path path() throws InvalidPathException {

        final var service = service().name() == null
                ? service().type()
                : "%s[%s]".formatted(service().type(), service().name());

        return element.path().appendComponents(service);

    }

    public RemoteElementMethodAddress withMethod(String name, String ... params) {
        return new RemoteElementMethodAddress(this, new ElementMethodAddress(service(), name, List.of(params)));
    }

}
