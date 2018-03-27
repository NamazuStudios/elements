package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.FCMRegistrationService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Supplier;

public class UserFCMRegistrationService implements FCMRegistrationService {

    private FCMRegistrationDao fcmRegistrationDao;

    private Supplier<Profile> currentProfileSupplier;

    @Override
    public FCMRegistration createRegistration(final FCMRegistration fcmRegistration) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (fcmRegistration.getProfile() == null) {
            fcmRegistration.setProfile(profile);
        } else if (!Objects.equals(fcmRegistration.getProfile(), profile.getId())) {
            throw new ForbiddenException("Profile mismatch when registering Firebase Token.");
        }

        return getFcmRegistrationDao().createRegistration(fcmRegistration);

    }

    @Override
    public FCMRegistration updateRegistration(final FCMRegistration fcmRegistration) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (fcmRegistration.getProfile() == null) {
            fcmRegistration.setProfile(profile);
        } else if (!Objects.equals(fcmRegistration.getProfile(), profile.getId())) {
            throw new ForbiddenException("Profile mismatch when registering Firebase Token.");
        }

        return getFcmRegistrationDao().updateRegistration(fcmRegistration);

    }

    @Override
    public void deleteRegistration(final String fcmRegistrationId) {
        final Profile profile = getCurrentProfileSupplier().get();
        getFcmRegistrationDao().deleteRegistrationWithRequestingProfile(profile, fcmRegistrationId);
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
