package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class ScheduleEventServiceProvider implements Provider<ScheduleEventService> {

    private User user;

    private Provider<SuperUserScheduleEventService> superUserScheduleEventServiceProvider;

    @Override
    public ScheduleEventService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserScheduleEventServiceProvider().get();
            default:
                return Services.forbidden(ScheduleEventService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserScheduleEventService> getSuperUserScheduleEventServiceProvider() {
        return superUserScheduleEventServiceProvider;
    }

    @Inject
    public void setSuperUserScheduleEventServiceProvider(Provider<SuperUserScheduleEventService> superUserScheduleEventServiceProvider) {
        this.superUserScheduleEventServiceProvider = superUserScheduleEventServiceProvider;
    }

}
