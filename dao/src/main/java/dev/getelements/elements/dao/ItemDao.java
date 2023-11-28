
package dev.getelements.elements.dao;

import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.List;

@Expose({
        @ModuleDefinition("eci.elements.dao.item"),
        @ModuleDefinition(
                value = "namazu.elements.dao.item",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.item instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.item",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.item instead")
        )
})
public interface ItemDao {

    /**
     * Retrieves a single Item from the database by its id property or name property.
     *
     * @param identifier
     *     Either an id or name of the Item
     * @return An representation of the Item as it exists in the database.
     * @throws dev.getelements.elements.exception.NotFoundException
     *     if the backing data store does not have an Item that is identified by the passed in identifier
     */
    Item getItemByIdOrName(String identifier);

    /**
     * Returns all Items in the subset of items filtered by tags and the given offset position.  The count parameter
     * should be greater than 0, but may also be overridden by a system defined limit on the number of records that can
     * be returned at once.
     *
     * @param offset
     *     the offset, which should be a positive integer
     * @param count
     *     a maximum amount of items to be returned, which should be a positive integer greater than 0
     * @param tags
     *     A set of tags to filter items by
     * @param category
     * @param query
     *     A search query to filter items by
     * @return A {@link Pagination} of all {@link Item} records within the range specified by the offset and count
     * parameters.
     */
    Pagination<Item> getItems(int offset, int count, List<String> tags, String category, String query);

    /**
     * Updates a given Item.
     *
     * @param item
     *     An Item to be updated.
     * @return The updated representation of the Item as it exists in the database
     * @throws InvalidDataException
     *     if the state of the passed in Item is invalid
     */
    Item updateItem(Item item);

    /**
     * Creates a new Item. All properties except for the Id property must be set to valid values. The name property must
     * also be unique in order for this call to succeed.
     *
     * @param item
     *     A new Item
     * @return A representation of the Item as it exists in the database after a successful write.
     * @throws InvalidDataException
     *     if the state of the passed in Item is invalid
     * @throws DuplicateException
     *     if the passed in Item has a name that already exists
     */
    Item createItem(Item item);
}
