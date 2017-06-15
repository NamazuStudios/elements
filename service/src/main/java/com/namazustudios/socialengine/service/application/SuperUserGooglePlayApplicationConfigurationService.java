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
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationConfigurationNameOrId) {
        getGooglePlayApplicationConfigurationDao().softDeleteApplicationProfile(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                          final String applicationConfigurationNameOrId) {
        return getGooglePlayApplicationConfigurationDao().getGooglePlayApplicationProfile(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                             final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getGooglePlayApplicationConfigurationDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, googlePlayApplicationConfiguration);
    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                             final String applicationConfigurationNameOrId,
                                                                             final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getGooglePlayApplicationConfigurationDao().updateApplicationProfile(applicationNameOrId, applicationConfigurationNameOrId, googlePlayApplicationConfiguration);
    }

    public GooglePlayApplicationConfigurationDao getGooglePlayApplicationConfigurationDao() {
        return googlePlayApplicationConfigurationDao;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationDao(GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao) {
        this.googlePlayApplicationConfigurationDao = googlePlayApplicationConfigurationDao;
    }

}
