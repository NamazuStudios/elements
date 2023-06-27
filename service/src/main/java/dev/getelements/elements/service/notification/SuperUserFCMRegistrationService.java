package dev.getelements.elements.service.notification;

import dev.getelements.elements.dao.FCMRegistrationDao;
import dev.getelements.elements.model.notification.FCMRegistration;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.FCMRegistrationService;
import dev.getelements.elements.service.NotificationService;

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
