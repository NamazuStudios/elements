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
public class ApplicationProfileEditorTableView extends ViewImpl implements ApplicationProfileEditorTablePresenter.MyView {

    interface ApplicationProfileEditorTableViewBinder extends UiBinder<Panel, ApplicationProfileEditorTableView> {}

    @UiField
    CellTable<ApplicationProfile> applicationProfileEditorCellTable;

    @UiField
    Pagination applicationProfileEditorCellTablePagination;

    @UiField
    TextBox applicationProfileSearchTextBox;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @Inject
    public ApplicationProfileEditorTableView(
            final ApplicationProfileEditorTableViewBinder applicationProfileEditorTableViewBinder) {
        initWidget(applicationProfileEditorTableViewBinder.createAndBindUi(this));
    }

}
