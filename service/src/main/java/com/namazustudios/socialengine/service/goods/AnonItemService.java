package com.namazustudios.socialengine.service.goods;

import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.service.ItemService;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public class AnonItemService implements ItemService {

    private ItemDao itemDao;

    @Override
    public Item getItemByIdOrName(String identifier) {
        return itemDao.getItemByIdOrName(identifier);
    }

    @Override
    public Pagination<Item> getItems(int offset, int count, List<String> tags, String query) {
        return itemDao.getItems(offset, count, tags, query);
    }

    @Override
    public Item updateItem(Item item) {
        throw new ForbiddenException("Unprivileged requests are unable to update items.");
    }

    @Override
    public Item createItem(Item item) {
        throw new ForbiddenException("Unprivileged requests are unable to create new items.");
    }

    @SuppressWarnings("unused")
    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    @SuppressWarnings("unused")
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }
}
