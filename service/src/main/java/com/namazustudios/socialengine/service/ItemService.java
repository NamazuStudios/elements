package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.List;

/**
 * Allows for accessing of the various {@link Item}s in the database.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.item"),
    @ExposedModuleDefinition(
        value = "namazu.elements.service.unscoped.item",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
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
    Item updateItem(Item item);

    /**
     * Creates a new {@link Item}.
     *
     * @param item the {@link Item} to create
     * @return the item, as it was written
     */
    Item createItem(Item item);

}
