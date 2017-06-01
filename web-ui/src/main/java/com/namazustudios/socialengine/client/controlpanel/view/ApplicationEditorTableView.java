package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/31/17.
 */
public class ApplicationEditorTableView extends ViewImpl implements ApplicationEditorTablePresenter.MyView {

    interface ApplicationEditorTableViewBinder extends UiBinder<Panel, ApplicationEditorTableView> {}

    @UiField
    CellTable<ApplicationProfile> applicationEditorCellTable;

    @UiField
    Pagination applicationEditorTablePagination;

    @UiField
    TextBox applicationsSearchTextBox;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @Inject
    public ApplicationEditorTableView(final ApplicationEditorTableViewBinder applicationEditorTableViewBinder) {
        initWidget(applicationEditorTableViewBinder.createAndBindUi(this));
    }

}
