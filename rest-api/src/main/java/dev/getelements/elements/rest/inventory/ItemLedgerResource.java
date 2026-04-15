package dev.getelements.elements.rest.inventory;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.sdk.service.inventory.ItemLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("inventory/ledger")
@Produces(MediaType.APPLICATION_JSON)
public class ItemLedgerResource {

    private ItemLedgerService itemLedgerService;

    @GET
    @Operation(summary = "Query the item audit ledger",
               description = "Returns paginated ledger entries for a specific inventory item or user. " +
                             "Exactly one of inventoryItemId or userId must be supplied.")
    public Pagination<ItemLedgerEntry> getLedgerEntries(
            @Parameter(description = "Filter entries for a specific inventory item ID.")
            @QueryParam("inventoryItemId") final String inventoryItemId,

            @Parameter(description = "Filter entries for a specific user ID (across all their inventory items).")
            @QueryParam("userId") final String userId,

            @Parameter(description = "Optional event type filter. When omitted, all event types are returned.")
            @QueryParam("eventType") final ItemLedgerEventType eventType,

            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count") @DefaultValue("20") final int count) {

        if (offset < 0) throw new InvalidParameterException("Offset must be non-negative.");
        if (count < 0) throw new InvalidParameterException("Count must be non-negative.");

        if (inventoryItemId != null && !inventoryItemId.isBlank()) {
            return getItemLedgerService().getLedgerEntries(inventoryItemId, offset, count, eventType);
        } else if (userId != null && !userId.isBlank()) {
            return getItemLedgerService().getLedgerEntriesForUser(userId, offset, count, eventType);
        } else {
            throw new InvalidParameterException("Either inventoryItemId or userId query parameter is required.");
        }
    }

    public ItemLedgerService getItemLedgerService() {
        return itemLedgerService;
    }

    @Inject
    public void setItemLedgerService(final ItemLedgerService itemLedgerService) {
        this.itemLedgerService = itemLedgerService;
    }
}
