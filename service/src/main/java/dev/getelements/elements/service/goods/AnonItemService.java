package dev.getelements.elements.service.goods;

import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.goods.CreateItemRequest;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.goods.UpdateItemRequest;
import dev.getelements.elements.service.ItemService;

import javax.inject.Inject;
import java.util.List;

public class AnonItemService implements ItemService {

    private ItemDao itemDao;

    @Override
    public Item getItemByIdOrName(String identifier) {
        return itemDao.getItemByIdOrName(identifier);
    }

    @Override
    public Pagination<Item> getItems(int offset, int count, List<String> tags, String category, String query) {
        return itemDao.getAllItems(offset, count, tags, category, query);
    }

    @Override
    public Pagination<Item> getPublicItems(int offset, int count, List<String> tags, String category, String query) {
        return itemDao.getPublicOnlyItems(offset, count, tags, category, query);
    }

    @Override
    public Item updateItem(String id, UpdateItemRequest item) {
        throw new ForbiddenException("Unprivileged requests are unable to update items.");
    }

    @Override
    public Item createItem(CreateItemRequest item) {
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
