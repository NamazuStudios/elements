package com.namazustudios.socialengine.model.goods;

/**
 * Identifies the category of an {@link Item} for sale.
 */
public enum ItemCategory {

    /**
     * Fungible items are equally valuable and interchangeable with each other. Fungible items apply to the simple and
     * advanced inventory types.
     */
    FUNGIBLE,

    /**
     * Distinct items are uniquely instantiated and are essentially copies of items derived from base digital goods that
     * reference back to the original item.
     */
    DISTINCT;

    /**
     * Gets the default item category.
     *
     * @return the default item category.
     */
    public static ItemCategory getDefault() {
        return FUNGIBLE;
    }

}
