package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.GooglePlayApplicationConfigurationService;
import jakarta.inject.Inject;

public class AnonGooglePlayApplicationConfigurationService implements GooglePlayApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationConfigurationNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public GooglePlayApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                          final String applicationConfigurationNameOrId) {
        final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration =
                getApplicationConfigurationDao().getApplicationConfiguration(
                        GooglePlayApplicationConfiguration.class,
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

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
