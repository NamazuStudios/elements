package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.GooglePlayApplicationProfileDao;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationProfile;
import com.namazustudios.socialengine.service.GooglePlayApplicationProfileService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserGooglePlayApplicationProfileService implements GooglePlayApplicationProfileService {

    private GooglePlayApplicationProfileDao googlePlayApplicationProfileDao;

    @Override
    public void deleteApplicationProfile(final String applicationNameOrId,
                                         final String applicationProfileNameOrId) {
        getGooglePlayApplicationProfileDao().softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public GooglePlayApplicationProfile getApplicationProfile(final String applicationNameOrId,
                                                              final String applicationProfileNameOrId) {
        return getGooglePlayApplicationProfileDao().getGooglePlayApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public GooglePlayApplicationProfile createApplicationProfile(final String applicationNameOrId,
                                                                 final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return getGooglePlayApplicationProfileDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, googlePlayApplicationProfile);
    }

    @Override
    public GooglePlayApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                                 final String applicationProfileNameOrId,
                                                                 final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return getGooglePlayApplicationProfileDao().updateApplicationProfile(applicationNameOrId, applicationProfileNameOrId, googlePlayApplicationProfile);
    }

    public GooglePlayApplicationProfileDao getGooglePlayApplicationProfileDao() {
        return googlePlayApplicationProfileDao;
    }

    @Inject
    public void setGooglePlayApplicationProfileDao(GooglePlayApplicationProfileDao googlePlayApplicationProfileDao) {
        this.googlePlayApplicationProfileDao = googlePlayApplicationProfileDao;
    }

}
