package dev.getelements.elements.service.inventory;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.inventory.ItemLedgerService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ItemLedgerServiceProvider implements Provider<ItemLedgerService> {

    private User user;

    private Provider<SuperuserItemLedgerService> superuserProvider;

    private Provider<AnonItemLedgerService> anonProvider;

    @Override
    public ItemLedgerService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperuserProvider().get();
            default:
                return getAnonProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(final User user) {
        this.user = user;
    }

    public Provider<SuperuserItemLedgerService> getSuperuserProvider() {
        return superuserProvider;
    }

    @Inject
    public void setSuperuserProvider(final Provider<SuperuserItemLedgerService> superuserProvider) {
        this.superuserProvider = superuserProvider;
    }

    public Provider<AnonItemLedgerService> getAnonProvider() {
        return anonProvider;
    }

    @Inject
    public void setAnonProvider(final Provider<AnonItemLedgerService> anonProvider) {
        this.anonProvider = anonProvider;
    }
}
