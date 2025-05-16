package dev.getelements.elements.rest.inventory;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.*;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.inventory.DistinctInventoryItemService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("inventory/distinct")
@Produces(MediaType.APPLICATION_JSON)
public class DistinctInventoryItemResource {

    private ValidationHelper validationHelper;

    private DistinctInventoryItemService advancedInventoryItemService;

    @GET
    @Path("{inventoryItemId}")
    @Operation( summary = "Gets inventory item for the specified item",
            description = "Gets the first (primary) inventory item for the specified item")
    public DistinctInventoryItem getDistinctInventoryItem(@PathParam("inventoryItemId") final String itemNameOrId) {
        return getDistinctInventoryItemService().getDistinctInventoryItem(itemNameOrId);
    }

    @GET
    @Operation(
            summary = "Search inventory items",
            description = "Searches all inventory items in the system and returns the metadata for all matches against " +
                    "the given search filter.")
    public Pagination<DistinctInventoryItem> getDistinctInventoryItems(
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an inventory item for the specified item",
            description = "Updates an inventory item for the specified item")
    public DistinctInventoryItem updateDistinctInventoryItem(
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create an inventory item for the specified item",
            description = "Create an inventory item for the specified item")
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
    @Operation(
            summary = "Delete the inventory item as identified by the given item name/id",
            description = "Delete the inventory item as identified by the given item name/id")
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
