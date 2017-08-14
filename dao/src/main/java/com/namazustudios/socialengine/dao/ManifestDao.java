package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.manifest.HttpApplicationManifest;

/**
 * Created by patricktwohig on 8/14/17.
 */
public interface ManifestDao {

    /**
     * Loads the {@link HttpApplicationManifest} for the supplied {@link Application} instance.
     *
     * @param application the {@link Application} for which to load the {@link HttpApplicationManifest}.
     *
     * @return the {@link HttpApplicationManifest}
     */
    HttpApplicationManifest getHttpManifestForApplication(Application application);

}
