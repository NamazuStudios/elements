package com.namazustudios.socialengine.model.goods;

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
    DISTINCT
}
