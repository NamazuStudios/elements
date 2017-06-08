package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.rest.client.ApplicationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.Platform;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.LabelType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.function.Consumer;

import static org.gwtbootstrap3.client.ui.constants.ValidationState.NONE;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationEditorView extends ViewImpl implements ApplicationEditorPresenter.MyView, Editor<Application> {

    interface Driver extends SimpleBeanEditorDriver<Application, ApplicationEditorView> {};

    interface ApplicationEditorViewBinder extends UiBinder<Container, ApplicationEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    FormGroup applicationNameFormGroup;

    @UiField
    @Path("name")
    TextBox applicationNameTextBox;

    @UiField
    @Ignore
    Label applicationNameWarningLabel;

    @UiField
    FormGroup applicationDescriptionFormGroup;

    @UiField
    @Path("name")
    TextBox applicationDescriptionTextBox;

    @UiField
    @Ignore
    Label descriptionWarningLabel;

    @UiField
    CellTable<ApplicationProfile> applicationProfileCellTable;

    @UiField
    Pagination applicationProfileCellTablePagination;

    @Inject
    private Driver driver;

    @Inject
    private Validator validator;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private PlaceManager placeManager;

    private Consumer<Application> save = a -> { lockOut(); createNewApplication(a);};

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public ApplicationEditorView(
            final ApplicationEditorViewBinder applicationEditorViewBinder,
            final ApplicationProfileDataProvider applicationProfileDataProvider) {

        initWidget(applicationEditorViewBinder.createAndBindUi(this));

        final Column<ApplicationProfile, String> profileIdColumn = new Column<ApplicationProfile, String>(new TextCell()) {
            @Override
            public String getValue(ApplicationProfile object) {
                return object.getId();
            }
        };

        final Column<ApplicationProfile, String> profilePlatformColumn = new Column<ApplicationProfile, String>(new TextCell()) {
            @Override
            public String getValue(ApplicationProfile object) {
                final Platform platform = object.getPlatform();
                return platform == null ? "" : platform.toString();
            }
        };

        final Column<ApplicationProfile, String> editColumn = new Column<ApplicationProfile, String>(new ButtonCell()) {
            @Override
            public String getValue(ApplicationProfile object) {
                return "Edit";
            }
        };

        editColumn.setFieldUpdater((index, object, value) -> {
            // TODO Implemnt delete and refresh table.
            Notify.notify("Todo!");
        });

        final Column<ApplicationProfile, String> deleteColumn = new Column<ApplicationProfile, String>(new ButtonCell()) {
            @Override
            public String getValue(ApplicationProfile object) {
                return "Delete";
            }
        };

        deleteColumn.setFieldUpdater(((index, object, value) -> {
            // TODO Implemnt delete and refresh table.
            Notify.notify("Todo!");
        }));

        applicationProfileCellTable.addColumn(profileIdColumn, "Proile ID");
        applicationProfileCellTable.addColumn(profilePlatformColumn, "Platform");
        applicationProfileCellTable.addColumn(editColumn);
        applicationProfileCellTable.addColumn(deleteColumn);

        applicationProfileCellTable.addRangeChangeHandler(event -> applicationProfileCellTablePagination.rebuild(simplePager));
        applicationProfileDataProvider.addRefreshListener(() -> applicationProfileCellTablePagination.rebuild(simplePager));

        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.INFO);
        emptyLabel.setText("No Application Profiles Exist");
        applicationProfileCellTable.setEmptyTableWidget(emptyLabel);

        simplePager.setDisplay(applicationProfileCellTable);
        applicationProfileCellTablePagination.clear();
        applicationProfileDataProvider.addDataDisplay(applicationProfileCellTable);

    }

    public void lockOut() {
        applicationNameTextBox.setEnabled(false);
        applicationDescriptionTextBox.setEnabled(false);
    }

    public void unlock() {
        applicationNameTextBox.setEnabled(true);
        applicationDescriptionTextBox.setEnabled(true);
    }

    @Override
    public void reset() {

        applicationNameTextBox.setEnabled(true);
        applicationDescriptionTextBox.setEnabled(true);

        applicationNameTextBox.setText("");
        applicationDescriptionTextBox.setText("");
        applicationNameFormGroup.setValidationState(NONE);
        applicationDescriptionFormGroup.setValidationState(NONE);

        applicationNameWarningLabel.setVisible(false);
        descriptionWarningLabel.setVisible(false);

        applicationProfileCellTablePagination.clear();

    }

    @Override
    public void createApplication() {

        reset();

        driver.initialize(this);
        driver.edit(new Application());

        save = a -> {
            lockOut();
            createNewApplication(a);
        };

    }

    @Override
    public void editApplication(Application application) {
        reset();

        driver.initialize(this);
        driver.edit(application);

        save = a -> {
            lockOut();
            updateExistingApplication(a);
        };
    }

    private void createNewApplication(final Application application) {
        applicationClient.createApplication(application, new MethodCallback<Application>() {

            @Override
            public void onFailure(Method method, Throwable exception) {
                unlock();
                errorModal.setErrorMessage("There was a problem creating the application.");
                errorModal.show();
            }

            @Override
            public void onSuccess(Method method, Application response) {

                unlock();

                Notify.notify("Successfully created new Application.");

                final PlaceRequest placeRequest = new PlaceRequest.Builder()
                        .nameToken(NameTokens.MAIN)
                        .build();

                placeManager.revealPlace(placeRequest);

            }

        });
    }

    private void updateExistingApplication(final Application application) {
        applicationClient.updateApplication(application.getId(), application, new MethodCallback<Application>() {

            @Override
            public void onFailure(Method method, Throwable exception) {
                unlock();
                errorModal.setErrorMessage("There was a updating the application.");
            }

            @Override
            public void onSuccess(Method method, Application response) {

                unlock();

                Notify.notify("Successfully updated application.");

                final PlaceRequest placeRequest = new PlaceRequest.Builder()
                        .nameToken(NameTokens.MAIN)
                        .build();

                placeManager.revealPlace(placeRequest);

            }

        });

    }

    @UiHandler("create")
    public void onClickCreate(final ClickEvent ev) {

        boolean failed = false;
        final Application application = driver.flush();

        if (!validator.validateProperty(application, "name").isEmpty()) {
            failed = true;
            applicationNameWarningLabel.setVisible(true);
            applicationNameFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            applicationNameWarningLabel.setVisible(false);
            applicationNameFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(application, "description").isEmpty()) {
            failed = true;
            descriptionWarningLabel.setVisible(true);
            applicationDescriptionFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            descriptionWarningLabel.setVisible(false);
            applicationDescriptionFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!failed) {
            save.accept(application);
        }

    }

}
