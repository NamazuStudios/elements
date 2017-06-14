package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.GooglePlayApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.service.GooglePlayApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserGooglePlayApplicationConfigurationService implements GooglePlayApplicationConfigurationService {

    private GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao;

    @Override
    public void deleteApplicationProfile(final String applicationNameOrId,
                                         final String applicationProfileNameOrId) {
        getGooglePlayApplicationConfigurationDao().softDeleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationProfile(final String applicationNameOrId,
                                                                    final String applicationProfileNameOrId) {
        return getGooglePlayApplicationConfigurationDao().getGooglePlayApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration createApplicationProfile(final String applicationNameOrId,
                                                                       final GooglePlayApplicationConfiguration googlePlayApplicationProfile) {
        return getGooglePlayApplicationConfigurationDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, googlePlayApplicationProfile);
    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationProfile(final String applicationNameOrId,
                                                                       final String applicationProfileNameOrId,
                                                                       final GooglePlayApplicationConfiguration googlePlayApplicationProfile) {
        return getGooglePlayApplicationConfigurationDao().updateApplicationProfile(applicationNameOrId, applicationProfileNameOrId, googlePlayApplicationProfile);
    }

    public GooglePlayApplicationConfigurationDao getGooglePlayApplicationConfigurationDao() {
        return googlePlayApplicationConfigurationDao;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationDao(GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao) {
        this.googlePlayApplicationConfigurationDao = googlePlayApplicationConfigurationDao;
    }

}
