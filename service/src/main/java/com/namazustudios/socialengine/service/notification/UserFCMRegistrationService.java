package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.FCMRegistrationService;
import com.namazustudios.socialengine.service.NotificationService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.String.format;

public class UserFCMRegistrationService implements FCMRegistrationService {

    private FCMRegistrationDao fcmRegistrationDao;

    private Supplier<Profile> currentProfileSupplier;

    @Override
    public FCMRegistration createRegistration(final FCMRegistration fcmRegistration) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (fcmRegistration.getProfile() == null) {
            fcmRegistration.setProfile(profile);
        } else if (!Objects.equals(fcmRegistration.getProfile().getId(), profile.getId())) {
            final String msg = format("'%s'!='%s'", fcmRegistration.getProfile().getId(), profile.getId());
            throw new ForbiddenException("Profile mismatch when creating Firebase Token " + msg);
        }

        final FCMRegistration registration = getFcmRegistrationDao().createRegistration(fcmRegistration);
        return registration;

    }

    @Override
    public FCMRegistration updateRegistration(final FCMRegistration fcmRegistration) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (fcmRegistration.getProfile() == null) {
            fcmRegistration.setProfile(profile);
        } else if (!Objects.equals(fcmRegistration.getProfile().getId(), profile.getId())) {
            final String msg = format("'%s'!='%s'", fcmRegistration.getProfile().getId(), profile.getId());
            throw new ForbiddenException("Profile mismatch when updating Firebase Token " + msg);
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
