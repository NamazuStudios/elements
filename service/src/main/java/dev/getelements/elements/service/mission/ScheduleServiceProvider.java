package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.mission.ScheduleService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

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
