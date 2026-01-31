package dev.getelements.elements.service.system;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.system.ElementDeploymentService;
import dev.getelements.elements.service.util.Services;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ElementDeploymentServiceProvider implements Provider<ElementDeploymentService> {

    private User user;

    private Provider<SuperUserElementDeploymentService> superUserElementDeploymentServiceProvider;

    @Override
    public ElementDeploymentService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperUserElementDeploymentServiceProvider().get();
            default -> Services.forbidden(SuperUserElementDeploymentService.class);
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserElementDeploymentService> getSuperUserElementDeploymentServiceProvider() {
        return superUserElementDeploymentServiceProvider;
    }

    @Inject
    public void setSuperUserElementDeploymentServiceProvider(Provider<SuperUserElementDeploymentService> superUserElementDeploymentServiceProvider) {
        this.superUserElementDeploymentServiceProvider = superUserElementDeploymentServiceProvider;
    }

}
