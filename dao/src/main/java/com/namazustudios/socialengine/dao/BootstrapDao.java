package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;

/**
 * Created by patricktwohig on 8/22/17.
 */
public interface BootstrapDao {

    /**
     * Bootstraps the supplied {@link Application}.  This will create the skeleton and manifests
     * for the supplied {@link Application}.
     *
     * Calling this must ensure that the subsequent calls to get manifest instances will return
     * a non-null object with the minimal information to use the {@link Application}.
     *
     * @param application the {@link Application} to bootstrap.
     */
    void bootstrap(Application application);

}
