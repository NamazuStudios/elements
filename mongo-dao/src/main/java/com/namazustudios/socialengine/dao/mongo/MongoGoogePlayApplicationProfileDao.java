package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.GooglePlayApplicationProfileDao;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationProfile;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoGoogePlayApplicationProfileDao implements GooglePlayApplicationProfileDao {

    @Override
    public GooglePlayApplicationProfile createOrUpdateInactiveApplicationProfile(
            final String applicationNameOrId,
            final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return null;
    }

    @Override
    public GooglePlayApplicationProfile getGooglePlayApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {
        return null;
    }

    @Override
    public GooglePlayApplicationProfile updateApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return null;
    }

    @Override
    public void softDeleteApplicationProfile(
            final String applicationNameOrId,
            final String applicationProfileNameOrId) {

    }

}
