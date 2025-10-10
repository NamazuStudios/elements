package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.MetadataSpecDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.UpdateItemRequest;
import dev.getelements.elements.rt.exception.BadRequestException;

import dev.getelements.elements.sdk.service.goods.ItemService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.FUNGIBLE;
import static java.util.Objects.isNull;

public class SuperuserItemService implements ItemService {

    private ItemDao itemDao;

    private MetadataSpecDao metadataSpecDao;

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
        item.setMetadataSpec(itemRequest.getMetadataSpec());
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
        item.setPublicVisible(itemRequest.isPublicVisible());

        Optional.ofNullable(itemRequest.getMetadataSpecId())
                .map(id -> getMetadataSpecDao()
                        .findActiveMetadataSpec(id)
                        .orElseThrow(() -> new BadRequestException("Unknown metadata spec id: " + id)))
                .ifPresent(item::setMetadataSpec);

        return getItemDao().createItem(item);

    }

    @Override
    public void deleteItem(String identifier) {
        getItemDao().deleteItem(identifier);
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

    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

}
