package com.namazustudios.socialengine.client.widget;

import org.gwtbootstrap3.client.ui.AnchorListItem;

/**
 * Created by patricktwohig on 5/6/15.
 */
public abstract class EnumAnchorListItem<EnumT extends Enum<EnumT>> extends AnchorListItem implements EnumEditorItem<EnumT> {

    private final Class<EnumT> enumTClass;

    private EnumT enumerant;

    public EnumAnchorListItem(Class<EnumT> enumTClass) {
        this.enumTClass = enumTClass;
    }

    @Override
    public Class<EnumT> getEnumClass() {
        return enumTClass;
    }

    @Override
    public EnumT getEnumerant() {
        return enumerant;
    }

    public void setEnumerant(EnumT enumerant) {
        this.enumerant = enumerant;
    }

    @Override
    public String getDisplayName() {
        return getText();
    }

}
