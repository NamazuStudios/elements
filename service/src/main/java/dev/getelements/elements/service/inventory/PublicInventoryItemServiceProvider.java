package dev.getelements.elements.service.inventory;

import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

public class PublicInventoryItemServiceProvider implements Provider<PublicInventoryItemService> {

    private User user;

    private Provider<UserPublicInventoryItemService> userPublicInventoryItemServiceProvider;

    private Provider<SuperUserPublicInventoryItemService> superUserPublicInventoryItemServiceProvider;

    @Override
    public PublicInventoryItemService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserPublicInventoryItemServiceProvider().get();
            case USER:
                return getUserPublicInventoryItemServiceProvider().get();
            default:
                return forbidden(PublicInventoryItemService.class);

        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserPublicInventoryItemService> getUserPublicInventoryItemServiceProvider() {
        return userPublicInventoryItemServiceProvider;
    }

    @Inject
    public void setUserPublicInventoryItemServiceProvider(Provider<UserPublicInventoryItemService> userPublicInventoryItemServiceProvider) {
        this.userPublicInventoryItemServiceProvider = userPublicInventoryItemServiceProvider;
    }

    public Provider<SuperUserPublicInventoryItemService> getSuperUserPublicInventoryItemServiceProvider() {
        return superUserPublicInventoryItemServiceProvider;
    }

    @Inject
    public void setSuperUserPublicInventoryItemServiceProvider(Provider<SuperUserPublicInventoryItemService> superUserPublicInventoryItemServiceProvider) {
        this.superUserPublicInventoryItemServiceProvider = superUserPublicInventoryItemServiceProvider;
    }
}
