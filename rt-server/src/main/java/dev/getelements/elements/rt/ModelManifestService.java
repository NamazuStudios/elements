package dev.getelements.elements.rt;

import dev.getelements.elements.rt.manifest.model.ModelManifest;

/**
 * Loads the {@link ModelManifest}.
 */
public interface ModelManifestService {

    /**
     * Returns the {@link ModelManifest}.
     *
     * @return the {@link ModelManifest}
     */
    ModelManifest getModelManifest();

}
