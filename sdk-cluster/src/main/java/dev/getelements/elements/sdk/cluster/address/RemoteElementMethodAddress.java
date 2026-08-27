package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementMethodAddress;
import dev.getelements.elements.sdk.cluster.id.HasInstanceId;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

public record RemoteElementMethodAddress(
        RemoteElementServiceAddress service,
        ElementMethodAddress method) implements AsPath {

    @Override
    public Path path() throws InvalidPathException {

        final var params = "%s[%s]".formatted(
                method().name(),
                String.join(",", method.parameters())
        );

        return service.path().appendComponents(params);

    }

}
