package dev.getelements.elements.service.index;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.IndexService;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class IndexServiceProvider implements Provider<IndexService> {

    private User user;

    private Provider<SuperUserIndexService> superUserIndexServiceProvider;

    @Override
    public IndexService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserIndexServiceProvider().get();
            default:
                return Services.forbidden(IndexService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserIndexService> getSuperUserIndexServiceProvider() {
        return superUserIndexServiceProvider;
    }

    @Inject
    public void setSuperUserIndexServiceProvider(Provider<SuperUserIndexService> superUserIndexServiceProvider) {
        this.superUserIndexServiceProvider = superUserIndexServiceProvider;
    }

}
