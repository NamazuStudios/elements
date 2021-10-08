package com.namazustudios.socialengine.rest.inventory;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.*;
import com.namazustudios.socialengine.service.AdvancedInventoryItemService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Path("inventory/advanced")
@Api(value = "Inventory",
        description =
                "Manages inventory allowing for multiple stacks of the same item.  Each item stack is placed in the " +
                "priority slot specified in the request. This is used in scenarios where multiple stacks of the same" +
                "item are required. It is considered a superset of the simple inventory APIs.",
        authorizations = {
                @Authorization(AUTH_BEARER),
                @Authorization(SESSION_SECRET),
                @Authorization(SOCIALENGINE_SESSION_SECRET)
        }
)
public class AdvancedInventoryItemResource {

    private ValidationHelper validationHelper;

    private AdvancedInventoryItemService advancedInventoryItemService;

    @GET
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Gets inventory item for the specified item",
            notes = "Gets the first (primary) inventory item for the specified item")
    public InventoryItem getAdvancedInventoryItem(@PathParam("itemNameOrId") final String itemNameOrId) {
        return getAdvancedInventoryItemService().getInventoryItem(itemNameOrId);
    }

    @GET
    @ApiOperation(value = "Search inventory items",
            notes = "Searches all inventory items in the system and returns the metadata for all matches against " +
                    "the given search filter.")
    public Pagination<InventoryItem> getAdvancedInventoryItems(
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
            getAdvancedInventoryItemService().getInventoryItems(offset, count, userId) :
            getAdvancedInventoryItemService().getInventoryItems(offset, count, userId, query);

    }

    @PATCH
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Adjust the quantity of the inventory item for the specified item.",
            notes = "Adjust the quantity of the first (primary) inventory item for the specified item.  This " +
                    "implicitly will create the InventoryItem if it does not exist.  The inventory item value")
    public InventoryItem adjustAdvancedInventoryItemQuantity(
            @PathParam("itemNameOrId")
            final String itemNameOrId,
            final AdvancedInventoryItemQuantityAdjustment simpleInventoryItemQuantityAdjustment) {

        getValidationHelper().validateModel(simpleInventoryItemQuantityAdjustment);

        return getAdvancedInventoryItemService().adjustInventoryItemQuantity(
                simpleInventoryItemQuantityAdjustment.getUserId(),
                itemNameOrId,
                simpleInventoryItemQuantityAdjustment.getQuantityDelta());

    }

    @POST
    @ApiOperation(value = "Create an inventory item for the specified item",
            notes = "Create an inventory item for the specified item")
    public InventoryItem createAdvancedInventoryItem(final CreateAdvancedInventoryItemRequest createSimpleInventoryItemRequest) {

        getValidationHelper().validateModel(createSimpleInventoryItemRequest);

        return getAdvancedInventoryItemService().createInventoryItem(
                createSimpleInventoryItemRequest.getUserId(),
                createSimpleInventoryItemRequest.getItemId(),
                createSimpleInventoryItemRequest.getQuantity());

    }

    @DELETE
    @Path("{inventoryItemId}")
    @ApiOperation(value = "Delete the inventory item as identified by the given item name/id",
            notes = "Delete the inventory item as identified by the given item name/id")
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
