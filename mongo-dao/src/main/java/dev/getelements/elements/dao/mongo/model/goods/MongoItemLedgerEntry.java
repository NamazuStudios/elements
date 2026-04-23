package dev.getelements.elements.dao.mongo.model.goods;

import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

/**
 * MongoDB entity backing {@link dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry}.
 * Records are append-only; no mutation after insert.
 */
@Entity(value = "item_ledger", useDiscriminator = false)
public class MongoItemLedgerEntry {

    @Id
    private ObjectId id;

    @Indexed
    private String inventoryItemId;

    private ItemCategory itemCategory;

    private String itemId;

    @Indexed
    private String userId;

    private String actorId;

    private ItemLedgerEventType eventType;

    @Indexed
    private Date timestamp;

    private Integer quantityBefore;

    private Integer quantityAfter;

    private Map<String, Object> metadataBefore;

    private Map<String, Object> metadataAfter;

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
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
