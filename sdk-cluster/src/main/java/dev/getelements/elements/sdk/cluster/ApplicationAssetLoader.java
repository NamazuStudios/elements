package dev.getelements.elements.sdk.cluster;

import dev.getelements.elements.sdk.cluster.id.DeploymentId;

import java.nio.file.Path;

/**
 * Loads the assets for a particular deployment as defined by the {@link DeploymentId}.
 */
public interface ApplicationAssetLoader {

    /**
     * Defers to {@link DeploymentId#forUniqueName(String)} to find the {@link DeploymentId}.
     *
     * @param applicationIdString the deployment id string
     * @return the asset path
     */
    default Path getAssetPath(String applicationIdString) {
        final var deploymentId = DeploymentId.forUniqueName(applicationIdString);
        return getAssetPath(deploymentId);
    }

    /**
     * Gets the asset {@link Path} for the supplied {@link DeploymentId}, performing any loading as needed.
     *
     * The returned {@link Path} will be a location on disk from which to load the application's executable code.
     *
     * @param deploymentId the {@link DeploymentId} instance
     * @return the {@link Path} to the loaded asset
     */
    Path getAssetPath(DeploymentId deploymentId);

}
