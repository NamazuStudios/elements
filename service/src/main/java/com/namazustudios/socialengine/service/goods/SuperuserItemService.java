package com.namazustudios.socialengine.service.goods;

import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.goods.ItemCategory;
import com.namazustudios.socialengine.service.ItemService;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.namazustudios.socialengine.model.goods.ItemCategory.FUNGIBLE;

public class SuperuserItemService implements ItemService {

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
        final var category = item.getCategory();
        if (category == null) item.setCategory(FUNGIBLE);
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
