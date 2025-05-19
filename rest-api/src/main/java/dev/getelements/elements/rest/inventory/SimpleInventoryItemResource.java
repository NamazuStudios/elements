package dev.getelements.elements.rest.inventory;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.CreateSimpleInventoryItemRequest;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.inventory.SimpleInventoryItemQuantityAdjustment;
import dev.getelements.elements.sdk.model.inventory.UpdateInventoryItemRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.inventory.SimpleInventoryItemService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("inventory/simple")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleInventoryItemResource {

    private ValidationHelper validationHelper;

    private SimpleInventoryItemService simpleInventoryItemService;

    @GET
    @Path("{inventoryItemId}")
    @Operation( summary = "Gets inventory item for the specified item",
                  description = "Gets the first (primary) inventory item for the specified item")
    public InventoryItem getSimpleInventoryItem(@PathParam("inventoryItemId") final String itemNameOrId) {
        return getSimpleInventoryItemService().getInventoryItem(itemNameOrId);
    }

    @GET
    @Operation( summary = "Search inventory items",
                  description = "Searches all inventory items in the system and returns the metadata for all matches against " +
                          "the given search filter.")
    public Pagination<InventoryItem> getSimpleInventoryItems(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("userId") final String userId,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getSimpleInventoryItemService().getInventoryItems(offset, count, userId, search);
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{inventoryItemId}")
    @Operation( summary = "Adjust the quantity of the inventory item for the specified item.",
                  description = "Adjust the quantity of the first (primary) inventory item for the specified item.  This " +
                          "implicitly will create the InventoryItem if it does not exist.  The inventory item value")
    public InventoryItem adjustSimpleInventoryItemQuantity(
            @PathParam("inventoryItemId") final String inventoryItemId,
            final SimpleInventoryItemQuantityAdjustment simpleInventoryItemQuantityAdjustment) {

        getValidationHelper().validateModel(simpleInventoryItemQuantityAdjustment);

        return getSimpleInventoryItemService().adjustInventoryItemQuantity(
                inventoryItemId,
                simpleInventoryItemQuantityAdjustment.getUserId(),
                simpleInventoryItemQuantityAdjustment.getQuantityDelta());

    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{inventoryItemId}")
    @Operation( summary = "Updates an inventory item for the specified item",
                  description = "Updates an inventory item for the specified item")
    public InventoryItem updateSimpleInventoryItem(
            @PathParam("inventoryItemId")
            final String inventoryItemId,
            final UpdateInventoryItemRequest updateInventoryItemRequest) {

        getValidationHelper().validateModel(updateInventoryItemRequest);

        return getSimpleInventoryItemService().updateInventoryItem(
            inventoryItemId,
            updateInventoryItemRequest.getQuantity());

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create an inventory item for the specified item",
            description = "Create an inventory item for the specified item")
    public InventoryItem createSimpleInventoryItem(final CreateSimpleInventoryItemRequest createSimpleInventoryItemRequest) {

        getValidationHelper().validateModel(createSimpleInventoryItemRequest);

        return getSimpleInventoryItemService().createInventoryItem(
            createSimpleInventoryItemRequest.getUserId(),
            createSimpleInventoryItemRequest.getItemId(),
            createSimpleInventoryItemRequest.getQuantity());

    }

    @DELETE
    @Path("{inventoryItemId}")
    @Operation(
            summary = "Delete the inventory item as identified by the given item name/id",
            description = "Delete the inventory item as identified by the given item name/id")
    public void deleteSimpleInventoryItem(@PathParam("inventoryItemId") final String inventoryItemId) {
        getSimpleInventoryItemService().deleteInventoryItem(inventoryItemId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public SimpleInventoryItemService getSimpleInventoryItemService() {
        return simpleInventoryItemService;
    }

    @Inject
    public void setSimpleInventoryItemService(SimpleInventoryItemService simpleInventoryItemService) {
        this.simpleInventoryItemService = simpleInventoryItemService;
    }

}
