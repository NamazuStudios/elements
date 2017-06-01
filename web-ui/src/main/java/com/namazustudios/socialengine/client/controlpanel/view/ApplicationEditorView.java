package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.Container;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationEditorView extends ViewImpl implements ApplicationEditorPresenter.MyView {

    interface ApplicationEditorViewBinder extends UiBinder<Container, ApplicationEditorView> {}

    @Inject
    public ApplicationEditorView(final ApplicationEditorViewBinder applicationEditorViewBinder) {
        initWidget(applicationEditorViewBinder.createAndBindUi(this));
    }

}
