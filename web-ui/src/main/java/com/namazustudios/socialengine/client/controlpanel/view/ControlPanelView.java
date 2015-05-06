package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtplatform.mvp.client.ViewImpl;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/1/15.
 */
public class ControlPanelView extends ViewImpl implements ControlPanelPresenter.MyView {

    interface ControlPanelViewUiBinder extends UiBinder<ScrollPanel, ControlPanelView> {}

    @UiField
    SimplePanel contentContainer;

    @Inject
    public ControlPanelView(ControlPanelViewUiBinder controlPanelViewUiBinder) {
        initWidget(controlPanelViewUiBinder.createAndBindUi(this));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == ControlPanelPresenter.SET_MAIN_CONTENT_TYPE) {
            contentContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

}
