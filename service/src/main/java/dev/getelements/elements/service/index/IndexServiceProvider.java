package dev.getelements.elements.service.index;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.index.IndexService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

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
