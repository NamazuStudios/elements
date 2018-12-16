package com.namazustudios.socialengine.rest.inventory;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.CreateInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItemQuantityAdjustment;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Path("inventory/simple")
@Api(value = "Inventory",
        description = "Manages inventory ensuring that there is a single stack of items per item availble.  Each " +
                      "item stack is placed in the zero priority slot.  This simplifies the manipulation of the " +
                      "inventory greatly as the items can be referenced by the item name or ID.",
        authorizations = {@Authorization(SESSION_SECRET)})
@Produces(MediaType.APPLICATION_JSON)
public class SimpleInventoryItemResource {

    private ValidationHelper validationHelper;

    private SimpleInventoryItemService simpleInventoryItemService;

    @GET
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Gets inventory item for the specified item",
                  notes = "Gets the first (primary) inventory item for the specified item")
    public InventoryItem getInventoryItem(@PathParam("itemNameOrId") final String itemNameOrId) {
        return getSimpleInventoryItemService().getInventoryItem(itemNameOrId);
    }

    @GET
    @ApiOperation(value = "Search inventory items",
                  notes = "Searches all inventory items in the system and returns the metadata for all matches against " +
                          "the given search filter.")
    public Pagination<InventoryItem> getInventoryItems(@QueryParam("offset") @DefaultValue("0") final int offset,
                                                       @QueryParam("count")  @DefaultValue("20") final int count,
                                                       @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getSimpleInventoryItemService().getInventoryItems(offset, count) :
            getSimpleInventoryItemService().getInventoryItems(offset, count, query);

    }

    @PATCH
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Adjust the quantity of the inventory item for the specified item.",
                  notes = "Adjust the quantity of the first (primary) inventory item for the specified item.  This " +
                          "implicitly will create the InventoryItem if it does not exist.  The inventory item value")
    public InventoryItem adjustInventoryItemQuantity(
            @PathParam("itemNameOrId")
            final String itemNameOrId,
            final InventoryItemQuantityAdjustment inventoryItemQuantityAdjustment) {
        getValidationHelper().validateModel(inventoryItemQuantityAdjustment);
        return getSimpleInventoryItemService().adjustInventoryItemQuantity(itemNameOrId, inventoryItemQuantityAdjustment.getQuantityDelta());
    }

    @POST
    @ApiOperation(value = "Create an inventory item for the specified item",
                  notes = "Create an inventory item for the specified item")
    public InventoryItem createInventoryItem(final CreateInventoryItem createInventoryItem) {

        getValidationHelper().validateModel(createInventoryItem);

        return getSimpleInventoryItemService().createInventoryItem(
            createInventoryItem.getUser(),
            createInventoryItem.getItem(),
            createInventoryItem.getQuantity());

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
