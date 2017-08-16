package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

/**
 * Created by patricktwohig on 8/13/17.
 */
public interface ManifestService {

    HttpManifest getHttpManifestForApplication(Application application);

}
