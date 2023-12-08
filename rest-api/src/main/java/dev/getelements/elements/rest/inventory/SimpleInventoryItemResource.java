package dev.getelements.elements.rest.inventory;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.CreateSimpleInventoryItemRequest;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.model.inventory.SimpleInventoryItemQuantityAdjustment;
import dev.getelements.elements.model.inventory.UpdateInventoryItemRequest;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.inventory.SimpleInventoryItemService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("inventory/simple")
@Api(value = "Inventory",
    description = "Manages inventory ensuring that there is a single stack of items per item availble.  Each " +
                  "item stack is placed in the zero priority slot.  This simplifies the manipulation of the " +
                  "inventory greatly as the items can be referenced by the item name or ID.",
    authorizations = {
        @Authorization(AuthSchemes.AUTH_BEARER),
        @Authorization(AuthSchemes.SESSION_SECRET),
        @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
    }
)
@Produces(MediaType.APPLICATION_JSON)
public class SimpleInventoryItemResource {

    private ValidationHelper validationHelper;

    private SimpleInventoryItemService simpleInventoryItemService;

    @GET
    @Path("{inventoryItemId}")
    @ApiOperation(value = "Gets inventory item for the specified item",
                  notes = "Gets the first (primary) inventory item for the specified item")
    public InventoryItem getSimpleInventoryItem(@PathParam("inventoryItemId") final String itemNameOrId) {
        return getSimpleInventoryItemService().getInventoryItem(itemNameOrId);
    }

    @GET
    @ApiOperation(value = "Search inventory items",
                  notes = "Searches all inventory items in the system and returns the metadata for all matches against " +
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
    @Path("{inventoryItemId}")
    @ApiOperation(value = "Adjust the quantity of the inventory item for the specified item.",
                  notes = "Adjust the quantity of the first (primary) inventory item for the specified item.  This " +
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
    @Path("{inventoryItemId}")
    @ApiOperation(value = "Updates an inventory item for the specified item",
                  notes = "Updates an inventory item for the specified item")
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
    @ApiOperation(value = "Create an inventory item for the specified item",
                  notes = "Create an inventory item for the specified item")
    public InventoryItem createSimpleInventoryItem(final CreateSimpleInventoryItemRequest createSimpleInventoryItemRequest) {

        getValidationHelper().validateModel(createSimpleInventoryItemRequest);

        return getSimpleInventoryItemService().createInventoryItem(
            createSimpleInventoryItemRequest.getUserId(),
            createSimpleInventoryItemRequest.getItemId(),
            createSimpleInventoryItemRequest.getQuantity());

    }

    @DELETE
    @Path("{inventoryItemId}")
    @ApiOperation(value = "Delete the inventory item as identified by the given item name/id",
                  notes = "Delete the inventory item as identified by the given item name/id")
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
