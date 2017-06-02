package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationEditorView extends ViewImpl implements ApplicationEditorPresenter.MyView {

    interface ApplicationEditorViewBinder extends UiBinder<Container, ApplicationEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    TextBox applicationNameTextBox;

    @UiField
    Label applicationNameWarningLabel;

    @UiField
    TextBox applicationDescriptionTextBox;

    @UiField
    Label applicationDescriptionWarningLabel;

    @UiField
    CellTable<ApplicationProfile> applicationProfileCellTable;

    @UiField
    Pagination applicationProfileCellTablePagination;

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public ApplicationEditorView(
            final ApplicationEditorViewBinder applicationEditorViewBinder) {
        initWidget(applicationEditorViewBinder.createAndBindUi(this));
    }


}
