package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.manifest.HttpApplicationManifest;

/**
 * Created by patricktwohig on 8/13/17.
 */
public interface ManifestService {

    HttpApplicationManifest getHttpManifestForApplication(Application application);

}
