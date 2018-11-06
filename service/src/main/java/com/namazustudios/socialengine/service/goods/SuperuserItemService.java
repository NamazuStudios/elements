package com.namazustudios.socialengine.service.goods;

import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.service.ItemService;

import javax.inject.Inject;
import java.util.Set;

public class SuperuserItemService implements ItemService {

    private ItemDao itemDao;

    @Override
    public Item getItemByIdOrName(String identifier) {
        return itemDao.getItemByIdOrName(identifier);
    }

    @Override
    public Pagination<Item> getItems(int offset, int count, Set<String> tags, String query) {
        return itemDao.getItems(offset, count, tags, query);
    }

    @Override
    public Item updateItem(Item item) {
        return itemDao.updateItem(item);
    }

    @Override
    public Item createItem(Item item) {
        return itemDao.createItem(item);
    }

    @SuppressWarnings("unused")
    public ItemDao getItemDao() {
        return itemDao;
    }

    @SuppressWarnings("unused")
    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }
}
