package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.GooglePlayApplicationConfigurationDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.service.GooglePlayApplicationConfigurationService;

import javax.inject.Inject;

public class AnonGooglePlayApplicationConfigurationService implements GooglePlayApplicationConfigurationService {

    private GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationConfigurationNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                          final String applicationConfigurationNameOrId) {
        final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration =
                getGooglePlayApplicationConfigurationDao().getApplicationConfiguration(
                        applicationNameOrId,
                        applicationConfigurationNameOrId
                );

        final GooglePlayApplicationConfiguration redactedGooglePlayApplicationConfiguration =
                redactGooglePlayApplicationConfiguration(googlePlayApplicationConfiguration);

        return redactedGooglePlayApplicationConfiguration;
    }

    private GooglePlayApplicationConfiguration redactGooglePlayApplicationConfiguration(
            GooglePlayApplicationConfiguration googlePlayApplicationConfiguration
    ) {
        if (googlePlayApplicationConfiguration != null) {
            googlePlayApplicationConfiguration.setJsonKey(null);
        }

        return googlePlayApplicationConfiguration;
    }

    @Override
    public GooglePlayApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                             final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public GooglePlayApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                             final String applicationConfigurationNameOrId,
                                                                             final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        throw new ForbiddenException();
    }

    public GooglePlayApplicationConfigurationDao getGooglePlayApplicationConfigurationDao() {
        return googlePlayApplicationConfigurationDao;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationDao(GooglePlayApplicationConfigurationDao googlePlayApplicationConfigurationDao) {
        this.googlePlayApplicationConfigurationDao = googlePlayApplicationConfigurationDao;
    }

}
