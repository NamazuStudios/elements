package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.dao.rt.RTManifestDao;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new GitLoaderModule());
        install(new LuaManifestLoaderModule());
        bind(ManifestDao.class).to(RTManifestDao.class);
    }

}
