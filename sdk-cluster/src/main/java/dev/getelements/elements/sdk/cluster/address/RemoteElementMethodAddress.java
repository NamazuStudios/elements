package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementMethodAddress;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * References a specific {@link dev.getelements.elements.sdk.address.ElementMethodAddress} on a remote element,
 * fully qualified with the {@link RemoteInstanceSelector} and service used to route the invocation.
 *
 * @param service the remote service address
 * @param method the method address
 */
public record RemoteElementMethodAddress(
        RemoteElementServiceAddress service,
        ElementMethodAddress method) implements AsPath {

    private static final Pattern METHOD_COMPONENT = Pattern.compile("^([^\\[\\]]+)\\[([^]]*)]$");

    @Override
    public Path path() throws InvalidPathException {

        final var params = "%s[%s]".formatted(
                method().name(),
                String.join(",", method.parameters())
        );

        return service.path().appendComponents(params);

    }

    /**
     * Parses a {@link RemoteElementMethodAddress} back out of the {@link Path} produced by {@link #path()}.
     *
     * @param path the {@link Path}
     * @return the {@link RemoteElementMethodAddress}
     * @throws InvalidPathException if the path does not encode a valid method address
     */
    public static RemoteElementMethodAddress fromPath(final Path path) throws InvalidPathException {

        final var component = path.getComponent(-1);
        final var matcher = METHOD_COMPONENT.matcher(component);

        if (!matcher.matches()) {
            throw new InvalidPathException("Invalid method address component: " + component);
        }

        final var name = matcher.group(1);
        final var parametersString = matcher.group(2);

        final List<String> parameters = parametersString.isEmpty()
                ? List.of()
                : Arrays.asList(parametersString.split(","));

        final var service = RemoteElementServiceAddress.fromPath(path.parent());

        return new RemoteElementMethodAddress(service, new ElementMethodAddress(service.service(), name, parameters));

    }

    /**
     * Returns a copy of this address with the {@link RemoteInstanceSelector}'s {@link InstanceId} replaced, leaving
     * the deployment, element, service and method routing information unchanged.
     *
     * @param instanceId the new {@link InstanceId}
     * @return the new {@link RemoteElementMethodAddress}
     */
    public RemoteElementMethodAddress withInstanceId(final InstanceId instanceId) {

        final var currentElement = service().element();
        final var currentInstance = currentElement.instance();

        final var newInstance = new RemoteInstanceSelector(instanceId, currentInstance.deploymentId());
        final var newElement = new RemoteElementAddress(newInstance, currentElement.address());
        final var newService = new RemoteElementServiceAddress(newElement, service().service());

        return new RemoteElementMethodAddress(newService, method());

    }

}
