package dev.getelements.elements.rest.inventory;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.AdvancedInventoryItemQuantityAdjustment;
import dev.getelements.elements.sdk.model.inventory.CreateAdvancedInventoryItemRequest;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.inventory.UpdateInventoryItemRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.inventory.AdvancedInventoryItemService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("inventory/advanced")
@Produces(MediaType.APPLICATION_JSON)
public class AdvancedInventoryItemResource {

    private ValidationHelper validationHelper;

    private AdvancedInventoryItemService advancedInventoryItemService;

    @GET
    @Path("{inventoryItemId}")
    @Operation(
            summary = "Gets inventory item for the specified item",
            description = "Gets the first (primary) inventory item for the specified item")
    public InventoryItem getAdvancedInventoryItem(@PathParam("inventoryItemId") final String itemNameOrId) {
        return getAdvancedInventoryItemService().getInventoryItem(itemNameOrId);
    }

    @GET
    @Operation(
            summary = "Search inventory items",
            description = "Searches all inventory items in the system and returns the metadata for all matches " +
                    "against the given search filter.")
    public Pagination<InventoryItem> getAdvancedInventoryItems(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("userId") final String userId,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive summary.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getAdvancedInventoryItemService().getInventoryItems(offset, count, userId) :
            getAdvancedInventoryItemService().getInventoryItems(offset, count, userId, query);

    }

    @PATCH
    @Path("{inventoryItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Adjust the quantity of the inventory item for the specified item.",
            description = "Adjust the quantity of the first (primary) inventory item for the specified item.  This " +
                    "implicitly will create the InventoryItem if it does not exist.  The inventory item value")
    public InventoryItem adjustAdvancedInventoryItemQuantity(
            @PathParam("inventoryItemId")
            final String inventoryItemId,
            final AdvancedInventoryItemQuantityAdjustment advancedInventoryItemQuantityAdjustment) {

        getValidationHelper().validateModel(advancedInventoryItemQuantityAdjustment);

        return getAdvancedInventoryItemService().adjustInventoryItemQuantity(
                inventoryItemId,
                advancedInventoryItemQuantityAdjustment.getQuantityDelta()
        );

    }

    @PUT
    @Path("{inventoryItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an inventory item for the specified item",
            description = "Updates an inventory item for the specified item")
    public InventoryItem updateSimpleInventoryItem(
            @PathParam("inventoryItemId")
            final String inventoryItemId,
            final UpdateInventoryItemRequest updateInventoryItemRequest) {

        getValidationHelper().validateModel(updateInventoryItemRequest);

        return getAdvancedInventoryItemService().updateInventoryItem(
            inventoryItemId,
            updateInventoryItemRequest.getQuantity());

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create an inventory item for the specified item",
            description = "Create an inventory item for the specified item")
    public InventoryItem createAdvancedInventoryItem(final CreateAdvancedInventoryItemRequest createAdvancedInventoryItemRequest) {

        getValidationHelper().validateModel(createAdvancedInventoryItemRequest);

        return getAdvancedInventoryItemService().createInventoryItem(
                createAdvancedInventoryItemRequest.getUserId(),
                createAdvancedInventoryItemRequest.getItemId(),
                createAdvancedInventoryItemRequest.getQuantity(),
                createAdvancedInventoryItemRequest.getPriority());

    }

    @DELETE
    @Path("{inventoryItemId}")
    @Operation(
            summary = "Delete the inventory item as identified by the given item name/id",
            description = "Delete the inventory item as identified by the given item name/id")
    public void deleteAdvancedInventoryItem(@PathParam("inventoryItemId") final String inventoryItemId) {
        getAdvancedInventoryItemService().deleteInventoryItem(inventoryItemId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public AdvancedInventoryItemService getAdvancedInventoryItemService() {
        return advancedInventoryItemService;
    }

    @Inject
    public void setAdvancedInventoryItemService(AdvancedInventoryItemService advancedInventoryItemService) {
        this.advancedInventoryItemService = advancedInventoryItemService;
    }

}
