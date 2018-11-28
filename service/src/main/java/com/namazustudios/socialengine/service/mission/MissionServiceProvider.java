package com.namazustudios.socialengine.service.mission;

import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

/**
 * Created by davidjbrooks on 11/27/2018.
 */
public class MissionServiceProvider implements Provider<MissionService> {

    private User user;

    private Provider<UserMissionService> userMissionServiceProvider;

    private Provider<SuperUserMissionService> superUserMissionServiceProvider;

    @Override
    public MissionService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserMissionServiceProvider().get();
            case USER:
                return getUserMissionServiceProvider().get();
            default:
                return forbidden(MissionService.class);

        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserMissionService> getUserMissionServiceProvider() {
        return userMissionServiceProvider;
    }

    @Inject
    public void setUserMissionServiceProvider(Provider<UserMissionService> anonApplicationServiceProvider) {
        this.userMissionServiceProvider = anonApplicationServiceProvider;
    }

    public Provider<SuperUserMissionService> getSuperUserMissionServiceProvider() {
        return superUserMissionServiceProvider;
    }

    @Inject
    public void setSuperUserMissionServiceProvider(Provider<SuperUserMissionService> superUserApplicationServiceProvider) {
        this.superUserMissionServiceProvider = superUserApplicationServiceProvider;
    }

}
