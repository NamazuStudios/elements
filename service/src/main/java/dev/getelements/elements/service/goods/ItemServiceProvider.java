package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.goods.ItemService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ItemServiceProvider implements Provider<ItemService> {

    @Inject
    private User user;
    @Inject
    private Provider<SuperuserItemService> superUserItemServiceProvider;
    @Inject
    private Provider<AnonItemService> unprivilegedItemServiceProvider;

    @Override
    public ItemService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserItemServiceProvider.get();
            default:
                return unprivilegedItemServiceProvider.get();
        }
    }
}
