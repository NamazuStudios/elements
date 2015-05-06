package com.namazustudios.socialengine.client.widget;

/**
 * Created by patricktwohig on 5/6/15.
 */
public interface EnumEditorItem<EnumT extends Enum<EnumT>> {

    /**
     * Returns the {@link Class} of the enum editor type.
     *
     * @return
     */
    Class<EnumT> getEnumClass();

    /**
     * Gets the enumerant for the item.
     *
     * @return
     */
    EnumT getEnumerant();

    /**
     * Gets the display-able
     *
     * @return
     */
    String getDisplayName();

}
