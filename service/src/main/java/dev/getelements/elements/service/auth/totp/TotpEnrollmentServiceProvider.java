package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.TotpEnrollmentService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.UNPRIVILEGED;

public class TotpEnrollmentServiceProvider implements Provider<TotpEnrollmentService> {

    private User user;

    private Provider<AnonTotpEnrollmentService> anonTotpEnrollmentService;

    private Provider<UserTotpEnrollmentService> userTotpEnrollmentService;

    @Override
    public TotpEnrollmentService get() {

        if (getUser().getLevel() == null || UNPRIVILEGED.equals(getUser().getLevel())) {
            return getAnonTotpEnrollmentService().get();
        }

        // Available identically to USER and SUPERUSER accounts -- TOTP is a personal account security
        // feature, not restricted by privilege level.
        return getUserTotpEnrollmentService().get();

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonTotpEnrollmentService> getAnonTotpEnrollmentService() {
        return anonTotpEnrollmentService;
    }

    @Inject
    public void setAnonTotpEnrollmentService(Provider<AnonTotpEnrollmentService> anonTotpEnrollmentService) {
        this.anonTotpEnrollmentService = anonTotpEnrollmentService;
    }

    public Provider<UserTotpEnrollmentService> getUserTotpEnrollmentService() {
        return userTotpEnrollmentService;
    }

    @Inject
    public void setUserTotpEnrollmentService(Provider<UserTotpEnrollmentService> userTotpEnrollmentService) {
        this.userTotpEnrollmentService = userTotpEnrollmentService;
    }

}
