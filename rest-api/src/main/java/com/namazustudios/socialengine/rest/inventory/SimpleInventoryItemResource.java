package com.namazustudios.socialengine.rest.inventory;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.CreateSimpleInventoryItemRequest;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.inventory.SimpleInventoryItemQuantityAdjustment;
import com.namazustudios.socialengine.model.inventory.UpdateInventoryItemRequest;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Path("inventory/simple")
@Api(value = "Inventory",
    description = "Manages inventory ensuring that there is a single stack of items per item availble.  Each " +
                  "item stack is placed in the zero priority slot.  This simplifies the manipulation of the " +
                  "inventory greatly as the items can be referenced by the item name or ID.",
    authorizations = {
        @Authorization(AUTH_BEARER),
        @Authorization(SESSION_SECRET),
        @Authorization(SOCIALENGINE_SESSION_SECRET)
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

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getSimpleInventoryItemService().getInventoryItems(offset, count, userId) :
            getSimpleInventoryItemService().getInventoryItems(offset, count, userId, query);

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
                updateInventoryItemRequest.getUserId(),
                updateInventoryItemRequest.getItemId(),
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
