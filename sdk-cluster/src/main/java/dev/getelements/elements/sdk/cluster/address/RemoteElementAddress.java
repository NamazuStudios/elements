package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementAddress;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.cluster.id.HasInstanceId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The remote element address. This consists of a remote element id.
 *
 * @param deploymentId
 * @param address
 */
public record RemoteElementAddress(
        InstanceId instanceId,
        DeploymentId deploymentId,
        ElementAddress address) implements AsPath, HasInstanceId {

    public RemoteElementAddress {
        requireNonNull(address, "address");
        requireNonNull(deploymentId, "deploymentId");
    }

    @Override
    public Path path() {

        final var context = instanceId == null
                ? Path.WILDCARD
                : instanceId.asString();

        final var address = address().isDefault()
                ? address().name()
                : "%s[%d]".formatted(address().name(), address().index());

        return Path.fromContextAndComponents(context, address);

    }

    @Override
    public Optional<InstanceId> findInstanceId() {
        return Optional.ofNullable(instanceId());
    }

    /**
     *
     * @param type
     * @return
     */
    public RemoteElementServiceAddress withService(final String type) {
        return new RemoteElementServiceAddress(this, address.withService(type));
    }

    public RemoteElementServiceAddress withService(final String type, final String name) {
        return new RemoteElementServiceAddress(this, address.withService(type, name));
    }

}
