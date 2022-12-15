package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;

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
