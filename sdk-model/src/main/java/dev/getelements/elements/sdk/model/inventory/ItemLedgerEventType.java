package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Classifies the event recorded in an {@link ItemLedgerEntry}.
 */
@Schema(description = "The type of lifecycle event recorded in an item ledger entry.")
public enum ItemLedgerEventType {

    /** An inventory item was created. */
    CREATED,

    /** The quantity of a fungible inventory item was adjusted by a delta. */
    QUANTITY_ADJUSTED,

    /** The quantity of a fungible inventory item was set to an absolute value. */
    QUANTITY_SET,

    /** An inventory item was deleted. */
    DELETED,

    /** The metadata of a distinct inventory item was updated. */
    METADATA_UPDATED,

    /** An item catalog entry was created. */
    ITEM_CREATED,

    /** An item catalog entry was updated. */
    ITEM_UPDATED,

    /** An item catalog entry was deleted. */
    ITEM_DELETED
}
