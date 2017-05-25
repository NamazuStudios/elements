package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.IosApplicationProfileDao;
import com.namazustudios.socialengine.model.application.IosApplicationProfile;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoIosApplicationProfileDao implements IosApplicationProfileDao {

    @Override
    public IosApplicationProfile createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final IosApplicationProfile iosApplicationProfile) {
        return null;
    }

    @Override
    public IosApplicationProfile getIosApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {
        return null;
    }

    @Override
    public IosApplicationProfile updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final IosApplicationProfile iosApplicationProfile) {
        return null;
    }

    @Override
    public void softDeleteApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

    }

}
