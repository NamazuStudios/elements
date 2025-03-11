package dev.getelements.elements.service.match;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.match.MatchService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

/**
 * Provider for the {@link MatchService}.
 *
 * Created by patricktwohig on 7/19/17.
 */
public class MatchServiceProvider implements Provider<MatchService> {

    private User user;

    private Provider<UserMatchService> userMatchServiceProvider;

    @Override
    public MatchService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserMatchServiceProvider().get();
            default:
                return forbidden(MatchService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserMatchService> getUserMatchServiceProvider() {
        return userMatchServiceProvider;
    }

    @Inject
    public void setUserMatchServiceProvider(Provider<UserMatchService> userMatchServiceProvider) {
        this.userMatchServiceProvider = userMatchServiceProvider;
    }

}
