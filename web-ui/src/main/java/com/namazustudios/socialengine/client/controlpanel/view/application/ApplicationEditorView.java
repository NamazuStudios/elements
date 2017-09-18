package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.Range;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.rest.client.ApplicationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
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

import static com.namazustudios.socialengine.client.controlpanel.view.application.FacebookApplicationConfigurationEditorPresenter.Param.application_id;
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
    ConfirmationModal confirmationModal;

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
    @Path("description")
    TextBox applicationDescriptionTextBox;

    @UiField
    @Ignore
    Label descriptionWarningLabel;

    @UiField
    @Path("scriptRepoUrl")
    TextBox scriptRepoUrlTextBox;

    @UiField
    CellTable<ApplicationConfiguration> applicationConfigurationCellTable;

    @UiField
    Pagination applicationConfigurationCellTablePagination;

    @UiField
    Row configurationsTableRow;

    @UiField
    Row addConfigurationDropDownRow;

    @UiField
    @Ignore
    Anchor swaggerJsonLink;

    @Inject
    private Driver driver;

    @Inject
    private Validator validator;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private ConfigurationUtils configurationUtils;

    private Consumer<Application> save = a -> { lockOut(); createNewApplication(a);};

    private final SimplePager simplePager = new SimplePager();

    private final ApplicationConfigurationDataProvider applicationConfigurationDataProvider;

    @Inject
    public ApplicationEditorView(
            final ApplicationEditorViewBinder applicationEditorViewBinder,
            final ApplicationConfigurationDataProvider applicationConfigurationDataProvider) {

        initWidget(applicationEditorViewBinder.createAndBindUi(this));

        final Column<ApplicationConfiguration, String> profileIdColumn = new Column<ApplicationConfiguration, String>(new TextCell()) {
            @Override
            public String getValue(ApplicationConfiguration object) {
                return object.getId();
            }
        };

        final Column<ApplicationConfiguration, String> profilePlatformColumn = new Column<ApplicationConfiguration, String>(new TextCell()) {
            @Override
            public String getValue(ApplicationConfiguration object) {
                final Platform platform = object.getPlatform();
                return platform == null ? "" : platform.toString();
            }
        };

        final Column<ApplicationConfiguration, String> profileUniqueIdentifierColumn = new Column<ApplicationConfiguration, String>(new TextCell()) {
            @Override
            public String getValue(ApplicationConfiguration object) {
                return object.getUniqueIdentifier();
            }
        };

        final Column<ApplicationConfiguration, String> editColumn = new Column<ApplicationConfiguration, String>(new ButtonCell()) {
            @Override
            public String getValue(ApplicationConfiguration object) {
                return "Edit";
            }
        };

        editColumn.setFieldUpdater((index, object, value) -> editConfiguration(object));

        final Column<ApplicationConfiguration, String> deleteColumn = new Column<ApplicationConfiguration, String>(new ButtonCell()) {
            @Override
            public String getValue(ApplicationConfiguration object) {
                return "Delete";
            }
        };

        deleteColumn.setFieldUpdater(((index, object, value) -> confirmDeleteConfiguration(object)));

        applicationConfigurationCellTable.addColumn(profileIdColumn, "Proile ID");
        applicationConfigurationCellTable.addColumn(profilePlatformColumn, "Platform");
        applicationConfigurationCellTable.addColumn(profileUniqueIdentifierColumn, "Unique Identifier");
        applicationConfigurationCellTable.addColumn(editColumn);
        applicationConfigurationCellTable.addColumn(deleteColumn);

        applicationConfigurationCellTable.addRangeChangeHandler(event -> applicationConfigurationCellTablePagination.rebuild(simplePager));
        applicationConfigurationDataProvider.addRefreshListener(() -> applicationConfigurationCellTablePagination.rebuild(simplePager));

        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.INFO);
        emptyLabel.setText("No Application Profiles Exist");
        applicationConfigurationCellTable.setEmptyTableWidget(emptyLabel);

        simplePager.setDisplay(applicationConfigurationCellTable);
        applicationConfigurationCellTablePagination.clear();
        applicationConfigurationDataProvider.addDataDisplay(applicationConfigurationCellTable);
        this.applicationConfigurationDataProvider = applicationConfigurationDataProvider;

    }

    private void editConfiguration(final ApplicationConfiguration configuration) {
        final Application application = driver.flush();
        configurationUtils.editConfiguration(application, configuration);
    }

    private void confirmDeleteConfiguration(final ApplicationConfiguration configuration) {


        confirmationModal.getErrorTextLabel().setText(
            "Are you sure you wish to delete " + configuration.getPlatform() + " configuration " +
            "with unique identifier " + configuration.getUniqueIdentifier()
        );

        confirmationModal.setOnConfirmHandler(() -> deleteConfiguration(configuration));
        confirmationModal.show();

    }

    private void deleteConfiguration(final ApplicationConfiguration configuration) {

        final Application application = driver.flush();

        lockOut();

        configurationUtils
                .deleteConfiguration(configuration)
                .perform(application.getId(), configuration.getId(), new MethodCallback<Void>() {

                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        unlock();
                        errorModal.setErrorMessage("There was a problem deleting the configuration.");
                        errorModal.show();
                        editApplication(application);
                    }

                    @Override
                    public void onSuccess(Method method, Void response) {
                        unlock();
                        Notify.notify("Successfully deleted " + configuration.getUniqueIdentifier());
                    }

                });
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

        applicationConfigurationCellTablePagination.clear();

        configurationsTableRow.setVisible(false);
        addConfigurationDropDownRow.setVisible(false);

        scriptRepoUrlTextBox.setEnabled(false);

    }

    @Override
    public void createApplication() {

        reset();

        driver.initialize(this);
        driver.edit(new Application());

        configurationsTableRow.setVisible(false);
        addConfigurationDropDownRow.setVisible(false);
        applicationConfigurationDataProvider.setParentApplication(null);

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
        configurationsTableRow.setVisible(true);
        addConfigurationDropDownRow.setVisible(true);
        applicationConfigurationDataProvider.setParentApplication(application);

        swaggerJsonLink.setText("OpenAPI Specification");
        swaggerJsonLink.setHref(application.getHttpDocumentationUrl());

        final Range range = new Range(0, applicationConfigurationCellTable.getVisibleRange().getLength());
        applicationConfigurationCellTable.setVisibleRangeAndClearData(range, true);

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
                editApplication(response);
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
                editApplication(response);
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

    @UiHandler("createFacebook")
    public void onClickCreateFacebookConfiguration(ClickEvent ev) {

        final Application application = driver.flush();

        final PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.APPLICATION_CONFIG_FACEBOOK_EDIT)
                .with(application_id.name(), application.getId())
                .build();

        placeManager.revealPlace(placeRequest);

    }

    @UiHandler("createIos")
    public void onClickCreateIosConfiguration(ClickEvent ev) {
        Notify.notify("Not implemented - iOS");
    }

    @UiHandler("createAndroidGooglePlay")
    public void onClickCreateAndroidGooglePLay(ClickEvent ev) {
        Notify.notify("Not implemented - Google Play");
    }

    @UiHandler("createPS4")
    public void onClickCreatePS4(ClickEvent ev) {
        Notify.notify("Not implemented - PS4");
    }

    @UiHandler("createVita")
    public void onClickCreateVita(ClickEvent ev) {
        Notify.notify("Not implemented - Vita");
    }

}
