package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementMethodAddress;
import dev.getelements.elements.sdk.address.ElementServiceAddress;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a remote address to a specific service exposed by a {@link RemoteElementAddress}.
 *
 * @param element the remote element address
 * @param service the service address
 */
public record RemoteElementServiceAddress(
        RemoteElementAddress element,
        ElementServiceAddress service) implements AsPath {

    private static final Pattern SERVICE_COMPONENT = Pattern.compile("^([^\\[\\]]+)(?:\\[([^]]*)])?$");

    @Override
    public Path path() throws InvalidPathException {

        final var service = service().name() == null
                ? service().type()
                : "%s[%s]".formatted(service().type(), service().name());

        return element.path().appendComponents(service);

    }

    /**
     * Parses a {@link RemoteElementServiceAddress} back out of the {@link Path} produced by {@link #path()}.
     *
     * @param path the {@link Path}
     * @return the {@link RemoteElementServiceAddress}
     * @throws InvalidPathException if the path does not encode a valid service address
     */
    public static RemoteElementServiceAddress fromPath(final Path path) throws InvalidPathException {

        final var component = path.getComponent(-1);
        final var matcher = SERVICE_COMPONENT.matcher(component);

        if (!matcher.matches()) {
            throw new InvalidPathException("Invalid service address component: " + component);
        }

        final var type = matcher.group(1);
        final var name = matcher.group(2);

        final var element = RemoteElementAddress.fromPath(path.parent());

        return new RemoteElementServiceAddress(element, new ElementServiceAddress(element.address(), type, name));

    }

    public RemoteElementMethodAddress withMethod(final String name, final List<String> params) {
        return new RemoteElementMethodAddress(this, new ElementMethodAddress(service(), name, params));
    }

    public RemoteElementMethodAddress withMethod(final String name, final String ... params) {
        return new RemoteElementMethodAddress(this, new ElementMethodAddress(service(), name, List.of(params)));
    }

}
