package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.goods.CreateItemRequest;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.goods.UpdateItemRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Allows for accessing of the various {@link Item}s in the database.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.item"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.item",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.item",
                deprecated = @DeprecationDefinition("Use eci.elements.service.item instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.item",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.item instead.")
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
     * Gets zero or more {@link Item}s from the database.
     *
     * @return the {@link Item}
     */
    Pagination<Item> getPublicItems(int offset, int count, List<String> tags, String category, String search);

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
}
