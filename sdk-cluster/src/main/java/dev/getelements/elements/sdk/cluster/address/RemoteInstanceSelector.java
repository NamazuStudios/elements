package dev.getelements.elements.sdk.cluster.address;

import dev.getelements.elements.sdk.address.ElementAddress;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.cluster.id.HasInstanceId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.path.AsPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

/**
 * Used to select the remote instance. This matches {@link DeploymentId} and {@link InstanceId} and accepts a null value
 * for each. In the case of a null {@link DeploymentId} we defer to the constant {@link DeploymentId#NULL_DEPLOYMENT_ID}
 * to indicate the root element registry on the remote. In the case of a null {@link InstanceId}, we then specify a
 * wildcard which will match any available instance that otherwise matches.
 *
 * @param instanceId the instance id
 * @param deploymentId the deployment id
 */
public record RemoteInstanceSelector(
        InstanceId instanceId,
        DeploymentId deploymentId) implements AsPath, HasInstanceId {

    public static final RemoteInstanceSelector DEFAULT = new RemoteInstanceSelector();

    public RemoteInstanceSelector() {
        this(null, null);
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public Path path() throws InvalidPathException {

        final var context = instanceId == null
                ? Path.WILDCARD
                : instanceId.asString();

        final var deployment = deploymentId == null
                ? DeploymentId.NULL_DEPLOYMENT_ID.asString()
                : deploymentId.asString();

        return Path.fromContextAndComponents(context, deployment);

    }

    /**
     * Parses a {@link RemoteInstanceSelector} back out of the {@link Path} produced by {@link #path()}.
     *
     * @param path the {@link Path}
     * @return the {@link RemoteInstanceSelector}
     * @throws InvalidPathException if the path does not encode a valid instance selector
     */
    public static RemoteInstanceSelector fromPath(final Path path) throws InvalidPathException {

        final var context = path.getContext();

        final var instanceId = context == null || Path.WILDCARD.equals(context)
                ? null
                : new InstanceId(context);

        final var deploymentComponent = path.getComponent(0);

        final var deploymentId = DeploymentId.NULL_DEPLOYMENT_ID.asString().equals(deploymentComponent)
                ? null
                : new DeploymentId(deploymentComponent);

        return new RemoteInstanceSelector(instanceId, deploymentId);

    }

    public RemoteElementAddress withElement(final String name, final int index) {
        return withElement(new ElementAddress(name, index));
    }

    public RemoteElementAddress withElement(final ElementAddress element) {
        return new RemoteElementAddress(this, element);
    }

}
