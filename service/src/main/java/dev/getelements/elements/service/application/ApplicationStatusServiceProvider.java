package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.system.ElementStatusService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class ApplicationStatusServiceProvider implements Provider<ElementStatusService> {

    private User user;

    private Provider<SuperUserElementStatusService> superUserApplicationServiceProvider;

    @Override
    public ElementStatusService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperUserApplicationServiceProvider().get();
            default -> forbidden(ElementStatusService.class);
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserElementStatusService> getSuperUserApplicationServiceProvider() {
        return superUserApplicationServiceProvider;
    }

    @Inject
    public void setSuperUserApplicationServiceProvider(Provider<SuperUserElementStatusService> superUserApplicationServiceProvider) {
        this.superUserApplicationServiceProvider = superUserApplicationServiceProvider;
    }

}
