package com.namazustudios.socialengine.rest.inventory;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.inventory.*;
import com.namazustudios.socialengine.service.inventory.DistinctInventoryItemService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Path("inventory/distinct")
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
@Produces(MediaType.APPLICATION_JSON)
public class DistinctInventoryItemResource {

    private ValidationHelper validationHelper;

    private DistinctInventoryItemService advancedInventoryItemService;

    @GET
    @Path("{inventoryItemId}")
    @ApiOperation(value = "Gets inventory item for the specified item",
            notes = "Gets the first (primary) inventory item for the specified item")
    public DistinctInventoryItem getDistinctInventoryItem(@PathParam("inventoryItemId") final String itemNameOrId) {
        return getDistinctInventoryItemService().getDistinctInventoryItem(itemNameOrId);
    }

    @GET
    @ApiOperation(value = "Search inventory items",
            notes = "Searches all inventory items in the system and returns the metadata for all matches against " +
                    "the given search filter.")
    public Pagination<InventoryItem> getDistinctInventoryItems(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("userId") final String userId,
            @QueryParam("profileId") final String profileId,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final var query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getDistinctInventoryItemService().getDistinctInventoryItems(offset, count, userId, profileId) :
            getDistinctInventoryItemService().getDistinctInventoryItems(offset, count, userId, profileId, query);

    }

    @PUT
    @Path("{distinctInventoryItemId}")
    @ApiOperation(value = "Updates an inventory item for the specified item",
            notes = "Updates an inventory item for the specified item")
    public DistinctInventoryItem updateSimpleInventoryItem(
            @PathParam("distinctInventoryItemId")
            final String distinctInventoryItemId,
            final UpdateDistinctInventoryItemRequest updateInventoryItemRequest) {

        getValidationHelper().validateModel(updateInventoryItemRequest);

        return getDistinctInventoryItemService().updateDistinctInventoryItem(
                distinctInventoryItemId,
                updateInventoryItemRequest.getUserId(),
                updateInventoryItemRequest.getProfileId(),
                updateInventoryItemRequest.getMetadata()
        );

    }

    @POST
    @ApiOperation(value = "Create an inventory item for the specified item",
            notes = "Create an inventory item for the specified item")
    public DistinctInventoryItem createDistinctInventoryItem(
                final CreateDistinctInventoryItemRequest createDistinctInventoryItemRequest) {

        getValidationHelper().validateModel(createDistinctInventoryItemRequest);

        return getDistinctInventoryItemService().createDistinctInventoryItem(
                createDistinctInventoryItemRequest.getUserId(),
                createDistinctInventoryItemRequest.getProfileId(),
                createDistinctInventoryItemRequest.getItemId(),
                createDistinctInventoryItemRequest.getMetadata()
        );

    }

    @DELETE
    @Path("{distinctInventoryItemId}")
    @ApiOperation(value = "Delete the inventory item as identified by the given item name/id",
            notes = "Delete the inventory item as identified by the given item name/id")
    public void deleteDistinctInventoryItem(@PathParam("distinctInventoryItemId") final String inventoryItemId) {
        getDistinctInventoryItemService().deleteInventoryItem(inventoryItemId);
    }

    public ValidationHelper getValidationHelper() {
            return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
            this.validationHelper = validationHelper;
    }

    public DistinctInventoryItemService getDistinctInventoryItemService() {
            return advancedInventoryItemService;
    }

    @Inject
    public void setDistinctInventoryItemService(DistinctInventoryItemService advancedInventoryItemService) {
            this.advancedInventoryItemService = advancedInventoryItemService;
    }

}
