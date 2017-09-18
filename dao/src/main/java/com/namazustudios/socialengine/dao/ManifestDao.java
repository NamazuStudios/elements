package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;

/**
 * Created by patricktwohig on 8/14/17.
 */
public interface ManifestDao {

    /**
     * Loads the {@link HttpManifest} for the supplied {@link Application} instance.
     *
     * @param application the {@link Application} for which to load the {@link HttpManifest}.
     *
     * @return the {@link HttpManifest}
     */
    HttpManifest getHttpManifestForApplication(Application application);

    /**
     * Loads the {@link ModelManifest} for the supplied {@link Application} instance.
     *
     * @param application the {@link Application} for which to load the {@link ModelManifest}.
     *
     * @return the {@link HttpManifest}
     */
    ModelManifest getModelManifestForApplication(Application application);

}
