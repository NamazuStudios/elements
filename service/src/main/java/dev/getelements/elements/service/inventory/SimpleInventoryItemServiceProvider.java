package dev.getelements.elements.service.inventory;

import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

/**
 * Created by davidjbrooks on 11/14/2018.
 */
public class SimpleInventoryItemServiceProvider implements Provider<SimpleInventoryItemService> {

    private User user;

    private Provider<UserSimpleInventoryItemService> userSimpleInventoryItemServiceProvider;

    private Provider<SuperUserSimpleInventoryItemService> superSimpleInventoryItemServiceProvider;

    @Override
    public SimpleInventoryItemService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserSimpleInventoryItemServiceProvider().get();
            case USER:
                return getUserSimpleInventoryItemServiceProvider().get();
            default:
                return forbidden(SimpleInventoryItemService.class);

        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserSimpleInventoryItemService> getUserSimpleInventoryItemServiceProvider() {
        return userSimpleInventoryItemServiceProvider;
    }

    @Inject
    public void setUserSimpleInventoryItemServiceProvider(Provider<UserSimpleInventoryItemService> anonApplicationServiceProvider) {
        this.userSimpleInventoryItemServiceProvider = anonApplicationServiceProvider;
    }

    public Provider<SuperUserSimpleInventoryItemService> getSuperUserSimpleInventoryItemServiceProvider() {
        return superSimpleInventoryItemServiceProvider;
    }

    @Inject
    public void setSuperUserSimpleInventoryItemServiceProvider(Provider<SuperUserSimpleInventoryItemService> superUserApplicationServiceProvider) {
        this.superSimpleInventoryItemServiceProvider = superUserApplicationServiceProvider;
    }

}
