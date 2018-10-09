package com.namazustudios.socialengine.service.goods;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.ItemService;

import javax.inject.Inject;
import javax.inject.Provider;

public class ItemServiceProvider implements Provider<ItemService> {

    @Inject
    private User user;
    @Inject
    private Provider<SuperuserItemService> superUserItemServiceProvider;
    @Inject
    private Provider<UnprivilegedItemService> unprivilegedItemServiceProvider;

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
