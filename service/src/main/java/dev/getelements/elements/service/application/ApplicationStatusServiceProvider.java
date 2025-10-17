package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.application.ApplicationStatusService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class ApplicationStatusServiceProvider implements Provider<ApplicationStatusService> {

    private User user;

    private Provider<ApplicationStatusService> superUserApplicationServiceProvider;

    @Override
    public ApplicationStatusService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperUserApplicationServiceProvider().get();
            default -> getSuperUserApplicationServiceProvider().get();
//            default -> forbidden(ApplicationStatusService.class);
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<ApplicationStatusService> getSuperUserApplicationServiceProvider() {
        return superUserApplicationServiceProvider;
    }

    @Inject
    public void setSuperUserApplicationServiceProvider(Provider<ApplicationStatusService> superUserApplicationServiceProvider) {
        this.superUserApplicationServiceProvider = superUserApplicationServiceProvider;
    }

}
