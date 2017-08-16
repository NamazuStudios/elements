package com.namazustudios.socialengine.service.manifest;

import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.service.ManifestService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class SuperUserManifestService implements ManifestService {

    private ManifestDao manifestDao;

    @Override
    public HttpManifest getHttpManifestForApplication(Application application) {
        return getManifestDao().getHttpManifestForApplication(application);
    }

    public ManifestDao getManifestDao() {
        return manifestDao;
    }

    @Inject
    public void setManifestDao(ManifestDao manifestDao) {
        this.manifestDao = manifestDao;
    }

}
