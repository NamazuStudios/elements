package dev.getelements.elements.rest.inventory;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.inventory.PublicInventoryItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("inventory/public")
@Api(value = "Inventory",
        description = "Manage inventory items that are marked public. Resource designed to read only.",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
        }
)
@Produces(MediaType.APPLICATION_JSON)
public class PublicInventoryItemResource {

    private PublicInventoryItemService publicInventoryItemService;

    @GET
    @ApiOperation(value = "Search public inventory items",
            notes = "Searches all inventory items in the system and returns the metadata for all matches against " +
                    "the given search filter.")
    public Pagination<InventoryItem> getPublicInventoryItems(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }


         return getPublicInventoryItemService().getPublicInventoryItems(offset, count);
    }

    public PublicInventoryItemService getPublicInventoryItemService() {
        return publicInventoryItemService;
    }

    @Inject
    public void setPublicInventoryItemService(PublicInventoryItemService publicInventoryItemService) {
        this.publicInventoryItemService = publicInventoryItemService;
    }
}
