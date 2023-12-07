package dev.getelements.elements.service.goods;

import dev.getelements.elements.dao.ItemDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.goods.CreateItemRequest;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.goods.UpdateItemRequest;
import dev.getelements.elements.service.ItemService;

import javax.inject.Inject;
import java.util.List;

import static dev.getelements.elements.model.goods.ItemCategory.FUNGIBLE;
import static java.util.Objects.isNull;

public class SuperuserItemService implements ItemService {

    private ItemDao itemDao;

    @Override
    public Item getItemByIdOrName(String identifier) {
        return getItemDao().getItemByIdOrName(identifier);
    }

    @Override
    public Pagination<Item> getItems(int offset, int count, List<String> tags, String category, String query) {
        return getItemDao().getItems(offset, count, tags, category, query);
    }

    @Override
    public Item updateItem(final String identifier, final UpdateItemRequest itemRequest) {
        final Item item = getItemByIdOrName(identifier);

        final var category = item.getCategory();
        if (category == null) item.setCategory(FUNGIBLE);

        item.setName(itemRequest.getName());
        item.setTags(itemRequest.getTags());
        item.setMetadata(itemRequest.getMetadata());
        item.setDescription(itemRequest.getDescription());
        item.setDisplayName(itemRequest.getDisplayName());
        item.setPublicVisible(!isNull(itemRequest.isPublicVisible()) && itemRequest.isPublicVisible());

        return getItemDao().updateItem(item);
    }

    @Override
    public Item createItem(CreateItemRequest itemRequest) {
        final Item item = new Item();
        item.setName(itemRequest.getName());
        item.setTags(itemRequest.getTags());
        item.setCategory(itemRequest.getCategory());
        item.setMetadata(itemRequest.getMetadata());
        item.setDescription(itemRequest.getDescription());
        item.setDisplayName(itemRequest.getDisplayName());
        item.setPublicVisible(!isNull(itemRequest.isPublicVisible()) && itemRequest.isPublicVisible());
        return getItemDao().createItem(item);
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
