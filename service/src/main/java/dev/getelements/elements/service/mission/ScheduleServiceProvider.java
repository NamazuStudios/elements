package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class ScheduleServiceProvider implements Provider<ScheduleService> {

    private User user;

    private Provider<SuperUserScheduleService> superUserScheduleServiceProvider;

    @Override
    public ScheduleService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserScheduleServiceProvider().get();
            default:
                return Services.forbidden(ScheduleService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserScheduleService> getSuperUserScheduleServiceProvider() {
        return superUserScheduleServiceProvider;
    }

    @Inject
    public void setSuperUserScheduleServiceProvider(Provider<SuperUserScheduleService> superUserScheduleServiceProvider) {
        this.superUserScheduleServiceProvider = superUserScheduleServiceProvider;
    }

}
