package dev.getelements.elements.service.goods;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ItemService;

import javax.inject.Inject;
import javax.inject.Provider;

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
