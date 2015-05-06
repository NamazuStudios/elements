package com.namazustudios.socialengine.client.widget;

import com.google.common.collect.Lists;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;


import java.util.List;

/**
 * Created by patricktwohig on 5/6/15.
 */
public abstract class EnumDropDown<EnumT extends Enum<EnumT> > extends DropDown implements LeafValueEditor<EnumT> {

    private final Class<EnumT> enumTClass;

    private final List<EnumT> enumTList;

    private final List<String> displayTextList;

    private EnumT value;

    private String defaultDisplayText;

    private Anchor anchor;

    private DropDownMenu dropDownMenu;

    public EnumDropDown(final Class<EnumT> enumTClass) {
        this.enumTClass = enumTClass;
        enumTList = Lists.newArrayList();
        displayTextList = Lists.newArrayList();
    }

    @Override
    public void setValue(EnumT value) {

        this.value = value;

        if (enumTList.contains(value) && value != null) {
            final int index = enumTList.indexOf(value);
            final String displayText = displayTextList.get(index);
            setAnchorText(displayText);
        } else {
            setAnchorText(defaultDisplayText);
        }

    }

    @Override
    public EnumT getValue() {
        return value;
    }

    public String getDefaultDisplayText() {
        return defaultDisplayText;
    }

    public void setDefaultDisplayText(String defaultDisplayText) {
        this.defaultDisplayText = defaultDisplayText;
    }

    @Override
    protected void onLoad() {

        super.onLoad();

        enumTList.clear();
        displayTextList.clear();

        anchor = findAnchor();
        dropDownMenu = findDropDownMenu();
        defaultDisplayText = getAnchorText();

        addHandlersToDropdownItems();

    }

    private Anchor findAnchor() {

        for (final Widget child : getChildren()) {
            if (child instanceof Anchor) {
                return ((Anchor) child);
            }
        }

        return null;

    }

    private DropDownMenu findDropDownMenu() {

        for (final Widget child : getChildren()) {
            if (child instanceof DropDownMenu) {
                return ((DropDownMenu) child);
            }
        }

        return null;

    }

    private void addHandlersToDropdownItems() {

        for (Widget child : dropDownMenu) {

            if (child instanceof EnumEditorItem) {
                final EnumEditorItem<EnumT> enumEditorItem = (EnumEditorItem<EnumT>)child;
                addEnumEditorItem(child, enumEditorItem);
            }

        }

    }

    private void addEnumEditorItem(final Widget child, EnumEditorItem<EnumT> enumEditorItem) {

        if (!enumTClass.getName().equals(enumEditorItem.getEnumClass().getName())) {
            throw new IllegalStateException("Editor for type not valid");
        }

        final EnumT enumerant = enumEditorItem.getEnumerant();

        final String displayText = enumEditorItem.getDisplayName() == null ?
                enumerant.toString() :
                enumEditorItem.getDisplayName();

        enumTList.add(enumerant);
        displayTextList.add(displayText);

        child.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                value = enumerant;
                setAnchorText(displayText);
            }

        }, ClickEvent.getType());

    }

    private String getAnchorText() {
        return anchor == null ? null : anchor.getText();
    }

    private void setAnchorText(final String anchorText) {
        if (anchor != null) {
            anchor.setText(anchorText);
        }
    }

}
