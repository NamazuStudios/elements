package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.service.FirebaseApplicationConfigurationService;

import javax.inject.Inject;

public class SuperUserFirebaseApplicationConfigurationService implements FirebaseApplicationConfigurationService {

    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        getFirebaseApplicationConfigurationDao()
            .softDeleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        return getFirebaseApplicationConfigurationDao()
            .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public FirebaseApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {
        return getFirebaseApplicationConfigurationDao()
            .createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, firebaseApplicationConfiguration);
    }

    @Override
    public FirebaseApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {
        return getFirebaseApplicationConfigurationDao()
            .updateApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId, firebaseApplicationConfiguration);
    }

    public FirebaseApplicationConfigurationDao getFirebaseApplicationConfigurationDao() {
        return firebaseApplicationConfigurationDao;
    }

    @Inject
    public void setFirebaseApplicationConfigurationDao(FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao) {
        this.firebaseApplicationConfigurationDao = firebaseApplicationConfigurationDao;
    }

}
