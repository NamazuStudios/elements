package dev.getelements.elements.sdk.model.inventory;

import dev.getelements.elements.sdk.model.goods.ItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Immutable audit record for a single inventory or item-catalog lifecycle event.
 */
@Schema(description = "An immutable audit record for a single inventory or item-catalog lifecycle event.")
public class ItemLedgerEntry {

    @Schema(description = "The unique ID of this ledger entry.")
    private String id;

    @Schema(description = "ID of the inventory item affected; null for item-catalog events.")
    private String inventoryItemId;

    @Schema(description = "The inventory category (FUNGIBLE or DISTINCT); null for item-catalog events.")
    private ItemCategory itemCategory;

    @Schema(description = "ID of the item definition.")
    private String itemId;

    @Schema(description = "ID of the user who owns the inventory item; null for item-catalog events.")
    private String userId;

    @Schema(description = "ID of the user who performed the action; null when triggered by a system or plugin without a user context.")
    private String actorId;

    @Schema(description = "The type of event recorded in this entry.")
    private ItemLedgerEventType eventType;

    @Schema(description = "Epoch milliseconds when the event occurred.")
    private long timestamp;

    @Schema(description = "Quantity before the change; populated for QUANTITY_ADJUSTED events only.")
    private Integer quantityBefore;

    @Schema(description = "Quantity after the change; populated for CREATED, QUANTITY_ADJUSTED, and QUANTITY_SET events.")
    private Integer quantityAfter;

    @Schema(description = "Metadata snapshot before the change; populated for METADATA_UPDATED events only.")
    private Map<String, Object> metadataBefore;

    @Schema(description = "Metadata snapshot after the change; populated for METADATA_UPDATED events only.")
    private Map<String, Object> metadataAfter;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(final String inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(final ItemCategory itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(final String actorId) {
        this.actorId = actorId;
    }

    public ItemLedgerEventType getEventType() {
        return eventType;
    }

    public void setEventType(final ItemLedgerEventType eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(final Integer quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(final Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public Map<String, Object> getMetadataBefore() {
        return metadataBefore;
    }

    public void setMetadataBefore(final Map<String, Object> metadataBefore) {
        this.metadataBefore = metadataBefore;
    }

    public Map<String, Object> getMetadataAfter() {
        return metadataAfter;
    }

    public void setMetadataAfter(final Map<String, Object> metadataAfter) {
        this.metadataAfter = metadataAfter;
    }
}
