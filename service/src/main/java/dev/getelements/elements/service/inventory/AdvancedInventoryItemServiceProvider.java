package dev.getelements.elements.service.inventory;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.AdvancedInventoryItemService;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

public class AdvancedInventoryItemServiceProvider implements Provider<AdvancedInventoryItemService> {

    private User user;

    private Provider<UserAdvancedInventoryItemService> userSimpleInventoryItemServiceProvider;

    private Provider<SuperUserAdvancedInventoryItemService> superSimpleInventoryItemServiceProvider;

    @Override
    public AdvancedInventoryItemService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserSimpleInventoryItemServiceProvider().get();
            case SUPERUSER:
                return getSuperSimpleInventoryItemServiceProvider().get();
            default:
                return forbidden(AdvancedInventoryItemService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserAdvancedInventoryItemService> getUserSimpleInventoryItemServiceProvider() {
        return userSimpleInventoryItemServiceProvider;
    }

    @Inject
    public void setUserSimpleInventoryItemServiceProvider(Provider<UserAdvancedInventoryItemService> userSimpleInventoryItemServiceProvider) {
        this.userSimpleInventoryItemServiceProvider = userSimpleInventoryItemServiceProvider;
    }

    public Provider<SuperUserAdvancedInventoryItemService> getSuperSimpleInventoryItemServiceProvider() {
        return superSimpleInventoryItemServiceProvider;
    }

    @Inject
    public void setSuperSimpleInventoryItemServiceProvider(Provider<SuperUserAdvancedInventoryItemService> superSimpleInventoryItemServiceProvider) {
        this.superSimpleInventoryItemServiceProvider = superSimpleInventoryItemServiceProvider;
    }

}
