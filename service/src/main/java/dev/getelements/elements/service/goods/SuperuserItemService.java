package dev.getelements.elements.service.goods;

import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.service.ItemService;

import javax.inject.Inject;
import java.util.List;

import static dev.getelements.elements.model.goods.ItemCategory.FUNGIBLE;

public class SuperuserItemService implements ItemService {

    private ItemDao itemDao;

    @Override
    public Item getItemByIdOrName(String identifier) {
        return itemDao.getItemByIdOrName(identifier);
    }

    @Override
    public Pagination<Item> getItems(int offset, int count, List<String> tags, String category, String query) {
        return itemDao.getItems(offset, count, tags, category, query);
    }

    @Override
    public Item updateItem(final Item item) {
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
