package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.Panel;

import javax.inject.Inject;

/**
 * Allows for the creation of a short link.
 *
 * Created by patricktwohig on 6/12/15.
 */
public class ShortLinkEditor extends ViewImpl implements ShortLinkEditorPresenter.MyView {

    interface ShortLinkEditorUiBinder extends UiBinder<Panel, ShortLinkEditor> {}

    @Inject
    public ShortLinkEditor(final ShortLinkEditorUiBinder shortLinkEditorUiBinder){
        initWidget(shortLinkEditorUiBinder.createAndBindUi(this));
    }

}
