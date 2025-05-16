package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.FirebaseApplicationConfigurationService;
import jakarta.inject.Inject;

public class SuperUserFirebaseApplicationConfigurationService implements FirebaseApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                FirebaseApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return getApplicationConfigurationDao().getApplicationConfiguration(
                FirebaseApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId
        );
    }

    @Override
    public FirebaseApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {
        return getApplicationConfigurationDao().createApplicationConfiguration(
                    applicationNameOrId,
                    firebaseApplicationConfiguration
            );
    }

    @Override
    public FirebaseApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {
        return getApplicationConfigurationDao().updateApplicationConfiguration(
                applicationNameOrId,
                firebaseApplicationConfiguration
        );
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }
}
