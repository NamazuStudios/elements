package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class ScheduleProgressServiceProvider implements Provider<ScheduleProgressService> {

    private User user;

    private Provider<UserScheduleProgressService> userScheduleProgressServiceProvider;

    @Override
    public ScheduleProgressService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserScheduleProgressServiceProvider().get();
            default:
                return Services.forbidden(ScheduleProgressService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserScheduleProgressService> getUserScheduleProgressServiceProvider() {
        return userScheduleProgressServiceProvider;
    }

    @Inject
    public void setUserScheduleProgressServiceProvider(Provider<UserScheduleProgressService> userScheduleProgressServiceProvider) {
        this.userScheduleProgressServiceProvider = userScheduleProgressServiceProvider;
    }

}
