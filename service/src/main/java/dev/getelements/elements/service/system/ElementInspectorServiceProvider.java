package dev.getelements.elements.service.system;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.system.ElementInspectorService;
import dev.getelements.elements.service.util.Services;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ElementInspectorServiceProvider implements Provider<ElementInspectorService> {

    private User user;

    private Provider<SuperUserElementInspectorService> superUserElementInspectorServiceProvider;

    @Override
    public ElementInspectorService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperUserElementInspectorServiceProvider().get();
            default -> Services.forbidden(ElementInspectorService.class);
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(final User user) {
        this.user = user;
    }

    public Provider<SuperUserElementInspectorService> getSuperUserElementInspectorServiceProvider() {
        return superUserElementInspectorServiceProvider;
    }

    @Inject
    public void setSuperUserElementInspectorServiceProvider(
            final Provider<SuperUserElementInspectorService> superUserElementInspectorServiceProvider) {
        this.superUserElementInspectorServiceProvider = superUserElementInspectorServiceProvider;
    }

}