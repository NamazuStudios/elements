package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;

/**
 * Manages loading of the various manifests for {@link Application} instances.
 *
 * Created by patricktwohig on 8/13/17.
 */
public interface ManifestService {

    /**
     * Gets the {@link HttpManifest} for the specified {@link Application}.  If this hasn't been done so,
     * the underlying application logic is loaded as calling this method.
     *
     * @param application the application
     * @return the {@link HttpManifest} for the {@link Application}
     */
    HttpManifest getHttpManifestForApplication(Application application);

    /**
     * Gets the {@link ModelManifest} for the specified {@link Application}.  If this hasn't been done so,
     * the underlying application logic is loaded as calling this method.
     *
     * @param application the application
     *
     * @return the {@link ModelManifest} for the {@link Application}
     */
    ModelManifest getModelManifestForApplication(Application application);

    /**
     * Gets the {@link SecurityManifest} for the specified {@link Application}.  If this hasn't been do so,
     * the underlying applicaiton logic is loaded by calling this method.
     *
     * @param application the application
     * @return the {@link SecurityManifest} for the {@link Application}
     */
    SecurityManifest getSecurityManifestForApplication(Application application);

}
