package dev.getelements.elements.sdk.service.goods;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.UpdateItemRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Allows for accessing of the various {@link Item}s in the database.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ItemService {

    /**
     * Get an {@link Item} by it's specific database-unique identifier.
     *
     * @param identifier the identifier
     * @return the {@link Item}
     */
    Item getItemByIdOrName(String identifier);

    /**
     * Gets zero or more {@link Item}s from the database.
     *
     * @return the {@link Item}
     */
    Pagination<Item> getItems(int offset, int count, List<String> tags, String category, String query);

    /**
     * Updates the specific {@link Item}.
     *
     * @param item the item to update
     * @return the item, as it was written
     */
    Item updateItem(String identifier, UpdateItemRequest item);

    /**
     * Creates a new {@link Item}.
     *
     * @param item the {@link Item} to create
     * @return the item, as it was written
     */
    Item createItem(CreateItemRequest item);

    /**
     * Soft deletes the item with the given identifier.
     * @param identifier Item name or id
     */
    void deleteItem(String identifier);
}
