package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by davidjbrooks on 11/27/2018.
 */
public class MissionServiceProvider implements Provider<MissionService> {

    private User user;

    private Provider<AnonMissionService> anonMissionServiceProvider;

    private Provider<SuperUserMissionService> superUserMissionServiceProvider;

    @Override
    public MissionService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserMissionServiceProvider().get();
            default:
                return getAnonMissionServiceProvider().get();
       }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonMissionService> getAnonMissionServiceProvider() {
        return anonMissionServiceProvider;
    }

    @Inject
    public void setAnonMissionServiceProvider(Provider<AnonMissionService> anonApplicationServiceProvider) {
        this.anonMissionServiceProvider = anonApplicationServiceProvider;
    }

    public Provider<SuperUserMissionService> getSuperUserMissionServiceProvider() {
        return superUserMissionServiceProvider;
    }

    @Inject
    public void setSuperUserMissionServiceProvider(Provider<SuperUserMissionService> superUserApplicationServiceProvider) {
        this.superUserMissionServiceProvider = superUserApplicationServiceProvider;
    }

}
