package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.Range;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.modal.OnConfirmHandler;
import com.namazustudios.socialengine.client.rest.client.ApplicationClient;
import com.namazustudios.socialengine.model.application.Application;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.LabelType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/31/17.
 */
public class ApplicationEditorTableView extends ViewImpl implements ApplicationEditorTablePresenter.MyView {

    interface ApplicationEditorTableViewBinder extends UiBinder<Panel, ApplicationEditorTableView> {}

    @UiField
    CellTable<Application> applicationEditorCellTable;

    @UiField
    Pagination applicationEditorTablePagination;

    @UiField
    TextBox applicationsSearchTextBox;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private PlaceManager placeManager;

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public ApplicationEditorTableView(
            final ApplicationEditorTableViewBinder applicationEditorTableViewBinder,
            final ApplicationDataProvider applicationDataProvider) {

        initWidget(applicationEditorTableViewBinder.createAndBindUi(this));

        final Column<Application, String> applicationIdColumn = new Column<Application, String>(new TextCell()) {
            @Override
            public String getValue(Application object) {
                return object.getId();
            }
        };

        final Column<Application, String> applicationNameColumn = new Column<Application, String>(new TextCell()) {
            @Override
            public String getValue(Application object) {
                return object.getName();
            }
        };

        applicationNameColumn.setFieldUpdater((index, object, value) -> {
            final String old = object.getName();
            object.setName(value);
            save(object, index, () -> object.setName(old));
        });

        final Column<Application, String> editColumn = new Column<Application, String>(new ButtonCell()) {
            @Override
            public String getValue(Application object) {
                return "Edit";
            }
        };

        editColumn.setFieldUpdater((index, object, value) -> {

            final PlaceRequest placeRequest = new PlaceRequest.Builder()
                    .nameToken(NameTokens.APPLICATION_EDIT)
                    .with(ApplicationEditorPresenter.Param.app.toString(), object.getId())
                    .build();

            placeManager.revealPlace(placeRequest);

        });

        final Column<Application, String> deleteColumn = new Column<Application, String>(new ButtonCell()) {
            @Override
            public String getValue(Application object) {
                return "Delete";
            }
        };

        deleteColumn.setFieldUpdater(((index, object, value) -> {
            confirmDelete(object);
        }));

        applicationEditorCellTable.addColumn(applicationIdColumn, "Id");
        applicationEditorCellTable.addColumn(applicationNameColumn, "Name");
        applicationEditorCellTable.addColumn(editColumn);
        applicationEditorCellTable.addColumn(deleteColumn);

        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.INFO);
        emptyLabel.setText("No applications match query.");
        applicationEditorCellTable.setEmptyTableWidget(emptyLabel);


        applicationEditorCellTable.addRangeChangeHandler(event -> applicationEditorTablePagination.rebuild(simplePager));
        applicationDataProvider.addRefreshListener(() -> applicationEditorTablePagination.rebuild(simplePager));

        simplePager.setDisplay(applicationEditorCellTable);
        applicationEditorTablePagination.clear();
        applicationDataProvider.addDataDisplay(applicationEditorCellTable);

        applicationsSearchTextBox.addChangeHandler(event -> {
            applicationDataProvider.setSearchFilter(applicationsSearchTextBox.getText());
            applicationEditorCellTable.setVisibleRangeAndClearData(applicationEditorCellTable.getVisibleRange(), true);
        });

    }

    private void save(final Application application, final int toRedraw, final Runnable unwwind) {
        applicationClient.updateApplication(application.getId(), application, new MethodCallback<Application>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                unwwind.run();
                applicationEditorCellTable.redrawRow(toRedraw);
                showErrorModal(throwable);
            }

            @Override
            public void onSuccess(Method method, Application user) {
                Notify.notify(user.getName() + " updated.");
                applicationEditorCellTable.redrawRow(toRedraw);
            }

        });
    }

    private void showErrorModal(final Throwable throwable) {
        errorModal.setMessageWithThrowable(throwable);
        errorModal.show();
    }

    private void confirmDelete(final Application application) {

        confirmationModal.getErrorTextLabel().setText("Delete application " + application.getName() + "?");
        confirmationModal.setOnConfirmHandler(new OnConfirmHandler() {

            @Override
            public void onConfirm() {
                delete(application);
            }

            @Override
            public void onDismiss() {}

        });

        confirmationModal.show();

    }

    private void delete(final Application application) {
        applicationClient.deleteApplication(application.getId(), new MethodCallback<Void>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                showErrorModal(throwable);
            }

            @Override
            public void onSuccess(Method method, Void aVoid) {
                Notify.notify(application.getName() + " deleted.");
                final Range range = applicationEditorCellTable.getVisibleRange();
                applicationEditorCellTable.setVisibleRangeAndClearData(range, true);
            }

        });
    }

}
