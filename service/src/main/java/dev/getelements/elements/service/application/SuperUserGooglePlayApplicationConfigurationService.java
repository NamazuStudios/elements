package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.GooglePlayApplicationConfigurationDao;
import dev.getelements.elements.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.service.GooglePlayApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class SuperUserGooglePlayApplicationConfigurationService implements GooglePlayApplicationConfigurationService {

    private GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationConfigurationNameOrId) {
        getGooglePlayApplicationConfigurationDao().softDeleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                          final String applicationConfigurationNameOrId) {
        return getGooglePlayApplicationConfigurationDao().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public GooglePlayApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                             final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getGooglePlayApplicationConfigurationDao().createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, googlePlayApplicationConfiguration);
    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                             final String applicationConfigurationNameOrId,
                                                                             final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getGooglePlayApplicationConfigurationDao().updateApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId, googlePlayApplicationConfiguration);
    }

    public GooglePlayApplicationConfigurationDao getGooglePlayApplicationConfigurationDao() {
        return googlePlayApplicationConfigurationDao;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationDao(GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao) {
        this.googlePlayApplicationConfigurationDao = googlePlayApplicationConfigurationDao;
    }

}
