package dev.getelements.elements.service.inventory;

import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

public class DistinctInventoryItemServiceProvider implements Provider<DistinctInventoryItemService> {

    private User user;

    private Provider<UserDistinctInventoryItemService> userDistinctInventoryItemServiceProvider;

    private Provider<SuperUserDistinctInventoryItemService> superUserAdvancedInventoryItemServiceProvider;

    @Override
    public DistinctInventoryItemService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserAdvancedInventoryItemServiceProvider().get();
            case USER:
                return getUserDistinctInventoryItemServiceProvider().get();
            default:
                return forbidden(DistinctInventoryItemService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserDistinctInventoryItemService> getUserDistinctInventoryItemServiceProvider() {
        return userDistinctInventoryItemServiceProvider;
    }

    @Inject
    public void setUserDistinctInventoryItemServiceProvider(Provider<UserDistinctInventoryItemService> userDistinctInventoryItemServiceProvider) {
        this.userDistinctInventoryItemServiceProvider = userDistinctInventoryItemServiceProvider;
    }

    public Provider<SuperUserDistinctInventoryItemService> getSuperUserAdvancedInventoryItemServiceProvider() {
        return superUserAdvancedInventoryItemServiceProvider;
    }

    @Inject
    public void setSuperUserAdvancedInventoryItemServiceProvider(Provider<SuperUserDistinctInventoryItemService> superUserAdvancedInventoryItemServiceProvider) {
        this.superUserAdvancedInventoryItemServiceProvider = superUserAdvancedInventoryItemServiceProvider;
    }

}
