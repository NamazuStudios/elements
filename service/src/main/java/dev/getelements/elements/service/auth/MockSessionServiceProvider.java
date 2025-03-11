package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.auth.MockSessionService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class MockSessionServiceProvider implements Provider<MockSessionService> {

    private User user;

    private Provider<SuperUserMockSessionService> superUserMockSessionServiceProvider;

    @Override
    public MockSessionService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER: return getSuperUserMockSessionServiceProvider().get();
            default:        return forbidden(MockSessionService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserMockSessionService> getSuperUserMockSessionServiceProvider() {
        return superUserMockSessionServiceProvider;
    }

    @Inject
    public void setSuperUserMockSessionServiceProvider(Provider<SuperUserMockSessionService> superUserMockSessionServiceProvider) {
        this.superUserMockSessionServiceProvider = superUserMockSessionServiceProvider;
    }

}
