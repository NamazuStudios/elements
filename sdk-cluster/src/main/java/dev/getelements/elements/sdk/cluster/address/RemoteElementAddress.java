package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementAddress;
import dev.getelements.elements.sdk.cluster.id.HasInstanceId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * The remote element address. This consists of a remote element id.
 *
 * @param instance the instance selector
 * @param address the element address
 */
public record RemoteElementAddress(
        RemoteInstanceSelector instance,
        ElementAddress address) implements AsPath, HasInstanceId {

    private static final Pattern ELEMENT_COMPONENT = Pattern.compile("^([^\\[\\]]+)(?:\\[(\\d+)])?$");

    public RemoteElementAddress {
        requireNonNull(address, "address");
        requireNonNull(instance, "instance");
    }

    @Override
    public Path path() {

        final var address = address().isDefault()
                ? address().name()
                : "%s[%d]".formatted(address().name(), address().index());

        return instance.path().appendComponents(address);

    }

    @Override
    public Optional<InstanceId> findInstanceId() {
        return Optional.ofNullable(instance().instanceId());
    }

    /**
     * Parses a {@link RemoteElementAddress} back out of the {@link Path} produced by {@link #path()}.
     *
     * @param path the {@link Path}
     * @return the {@link RemoteElementAddress}
     * @throws InvalidPathException if the path does not encode a valid element address
     */
    public static RemoteElementAddress fromPath(final Path path) throws InvalidPathException {

        final var component = path.getComponent(-1);
        final var matcher = ELEMENT_COMPONENT.matcher(component);

        if (!matcher.matches()) {
            throw new InvalidPathException("Invalid element address component: " + component);
        }

        final var name = matcher.group(1);

        final var index = matcher.group(2) == null
                ? ElementAddress.DEFAULT_INDEX
                : Integer.parseInt(matcher.group(2));

        final var instance = RemoteInstanceSelector.fromPath(path.parent());

        return new RemoteElementAddress(instance, new ElementAddress(name, index));

    }

    /**
     * Specifies an unnamed service exposed by this element.
     *
     * @param type the service type
     * @return the {@link RemoteElementServiceAddress}
     */
    public RemoteElementServiceAddress withService(final String type) {
        return new RemoteElementServiceAddress(this, address.withService(type));
    }

    /**
     * Specifies a named service exposed by this element.
     *
     * @param type the service type
     * @param name the service name
     * @return the {@link RemoteElementServiceAddress}
     */
    public RemoteElementServiceAddress withService(final String type, final String name) {
        return new RemoteElementServiceAddress(this, address.withService(type, name));
    }

}
