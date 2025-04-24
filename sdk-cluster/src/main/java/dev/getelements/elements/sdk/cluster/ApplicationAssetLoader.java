package dev.getelements.elements.sdk.cluster;

import dev.getelements.elements.sdk.cluster.id.ApplicationId;

import java.nio.file.Path;

/**
 * Loads the assets for a particular application as defined by the {@link ApplicationId}.
 */
public interface ApplicationAssetLoader {

    /**
     * The name of the configuration variable for the script storage directory.
     */
    String ELEMENT_STORAGE = "dev.getelements.elements.element.storage";

    /**
     * Defers to {@link ApplicationId#forUniqueName(String)} to find the {@link ApplicationId}.
     *
     * @param applicationIdString the application id string
     * @return the asset path
     */
    default Path getAssetPath(String applicationIdString) {
        final var applicationId = ApplicationId.forUniqueName(applicationIdString);
        return getAssetPath(applicationId);
    }

    /**
     * Gets the asset {@link Path} for the supplied {@link ApplicationId}, performing any loading as needed.
     *
     * The returned {@link Path} will be a location on disk from which to load the application's executable code.
     *
     * @param applicationId the {@link ApplicationId} instance
     * @return the {@link Path} to the loaded asset
     */
    Path getAssetPath(ApplicationId applicationId);

}
