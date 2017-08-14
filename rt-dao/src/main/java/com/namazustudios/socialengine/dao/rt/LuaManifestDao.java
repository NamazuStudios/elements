package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.manifest.HttpApplicationManifest;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class LuaManifestDao implements ManifestDao {

    @Override
    public HttpApplicationManifest getHttpManifestForApplication(Application application) {
        return null;
    }

}
