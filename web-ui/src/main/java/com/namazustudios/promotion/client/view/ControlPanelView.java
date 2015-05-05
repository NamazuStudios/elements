package com.namazustudios.promotion.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.Panel;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/1/15.
 */
public class ControlPanelView extends ViewImpl implements ControlPanelPresenter.MyView {

    interface ControlPanelViewUiBinder extends UiBinder<ScrollPanel, ControlPanelView> {}

    @Inject
    public ControlPanelView(ControlPanelViewUiBinder controlPanelViewUiBinder) {
        initWidget(controlPanelViewUiBinder.createAndBindUi(this));
    }

}
