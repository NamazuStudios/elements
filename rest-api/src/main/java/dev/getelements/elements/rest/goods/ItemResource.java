package dev.getelements.elements.rest.goods;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.UpdateItemRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.goods.ItemService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("item")
@Produces(MediaType.APPLICATION_JSON)
public class ItemResource {

    private ItemService itemService;

    private ValidationHelper validationHelper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates a new digital Item",
        description = "Supplying an item object, this will create a new item with a newly assigned unique id.  " +
                "The Item representation returned in the response body is a representation of the Item as persisted " +
                "with a unique identifier signed and with its fields properly normalized.  The supplied item object " +
                "submitted with the request must have a name property that is unique across all items.")
    public Item createItem(final CreateItemRequest itemToBeCreated) {

        getValidationHelper().validateModel(itemToBeCreated);
        return getItemService().createItem(itemToBeCreated);
    }

    @GET
    @Operation( summary = "Retrieves all Items",
        description = "Searches all items and returns all matching items, filtered by the passed in search parameters.  " +
                "If multiple tags are specified, then all items that contain at least one of the passed in tags is " +
                "returned.")
    public Pagination<Item> getItems(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count") @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags,
            @QueryParam("category") final String category,
            @QueryParam("search") final String search) {
        return getItemService().getItems(offset, count, tags, category, search);
    }
    @GET
    @Path("{identifier}")
    @Operation( summary = "Retrieves a single Item by id or by name",
        description = "Looks up an item by the passed in identifier")
    public Item getItemByIdentifier(
            @PathParam("identifier")
            final String identifier) {
        return getItemService().getItemByIdOrName(identifier);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{identifier}")
    @Operation( summary = "Updates a single Item",
        description = "Supplying an item, this will update the Item identified by the identifier in the path with contents " +
                "from the passed in request body. ")
    public Item updateItem(
            @PathParam("identifier")
            final String identifier,
            final UpdateItemRequest updateItemRequest) {

        getValidationHelper().validateModel(updateItemRequest);
        return getItemService().updateItem(identifier, updateItemRequest);

    }

    @DELETE
    @Path("{identifier}")
    @Operation(
            summary = "Delete the item as identified by the given item name/id",
            description = "Delete the item as identified by the given item name/id")
    public void deleteSimpleInventoryItem(@PathParam("identifier") final String identifier) {
        getItemService().deleteItem(identifier);
    }

    public ItemService getItemService() {
        return itemService;
    }

    @Inject
    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
