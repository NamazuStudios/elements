package com.namazustudios.socialengine.rest.inventory;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.CreateInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItemQuantityAdjustment;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemService;
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
        description = "Manages inventory",
        authorizations = {@Authorization(SESSION_SECRET)})
@Produces(MediaType.APPLICATION_JSON)
public class SimpleInventoryItemResource {

    private SimpleInventoryItemService simpleInventoryItemService;

    @GET
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Gets inventory item for the specified item",
        notes = "Gets the first (primary) inventory item for the specified item")
    public InventoryItem getInventoryItem(@PathParam("itemNameOrId") final String itemNameOrId) {
        return simpleInventoryItemService.getInventoryItem(itemNameOrId);
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

        return simpleInventoryItemService.getInventoryItems(offset, count, search);
    }

    @PATCH
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Adjust the quantity of the inventory item for the specified item",
            notes = "Adjust the quantity of the first (primary) inventory item for the specified item")
    public InventoryItem adjustInventoryItemQuantity(@PathParam("itemNameOrId") final String itemNameOrId,
             InventoryItemQuantityAdjustment inventoryItemQuantityAdjustment) {

        if(null == inventoryItemQuantityAdjustment) {
            throw new InvalidParameterException("InventoryItemQuantityAdjustment can not be null.");
        }

        return simpleInventoryItemService.adjustInventoryItemQuantity(itemNameOrId, inventoryItemQuantityAdjustment.getQuantityDelta());
    }

    @POST
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Create an inventory item for the specified item",
            notes = "Create an inventory item for the specified item")
    public InventoryItem createInventoryItem(@PathParam("itemNameOrId") final String itemNameOrId,
                                             CreateInventoryItem createInventoryItem) {

        if(null == createInventoryItem) {
            throw new InvalidParameterException("CreateInventoryItem can not be null.");
        }

        return simpleInventoryItemService.createInventoryItem(itemNameOrId, createInventoryItem.getQuantity());
    }

    @DELETE
    @Path("{itemNameOrId}")
    @ApiOperation(value = "Delete the inventory item for the specified item",
            notes = "Delete the first (primary) inventory item for the specified item")
    public void deleteInventoryItem(@PathParam("itemNameOrId") final String itemNameOrId) {
        simpleInventoryItemService.deleteInventoryItem(itemNameOrId);
    }

    public SimpleInventoryItemService getSimpleInventoryItemService() {
        return simpleInventoryItemService;
    }

    @Inject
    public void setSimpleInventoryItemService(SimpleInventoryItemService simpleInventoryItemService) {
        this.simpleInventoryItemService = simpleInventoryItemService;
    }
}
