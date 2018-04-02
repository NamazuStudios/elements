package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.FCMRegistrationService;
import com.namazustudios.socialengine.service.NotificationService;

import javax.inject.Inject;
import java.util.function.Supplier;

public class SuperUserFCMRegistrationService implements FCMRegistrationService {

    private FCMRegistrationDao fcmRegistrationDao;

    private Supplier<Profile> currentProfileSupplier;

    @Override
    public FCMRegistration createRegistration(final FCMRegistration fcmRegistration) {

        if (fcmRegistration.getProfile() == null) {
            fcmRegistration.setProfile(getCurrentProfileSupplier().get());
        }

        return getFcmRegistrationDao().createRegistration(fcmRegistration);

    }

    @Override
    public FCMRegistration updateRegistration(final FCMRegistration fcmRegistration) {

        if (fcmRegistration.getProfile() == null) {
            fcmRegistration.setProfile(getCurrentProfileSupplier().get());
        }

        return getFcmRegistrationDao().updateRegistration(fcmRegistration);

    }

    @Override
    public void deleteRegistration(final String fcmRegistrationId) {
        getFcmRegistrationDao().deleteRegistration(fcmRegistrationId);
    }

    public FCMRegistrationDao getFcmRegistrationDao() {
        return fcmRegistrationDao;
    }

    @Inject
    public void setFcmRegistrationDao(FCMRegistrationDao fcmRegistrationDao) {
        this.fcmRegistrationDao = fcmRegistrationDao;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

}
