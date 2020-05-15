package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.MockSessionService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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
