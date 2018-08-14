package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.rest.client.internal.ApplicationClient;
import com.namazustudios.socialengine.client.rest.client.internal.FirebaseApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;

public class FirebaseApplicationConfigurationEditorView extends ViewImpl implements
        Editor<FirebaseApplicationConfiguration>,
        FirebaseApplicationConfigurationEditorPresenter.MyView  {

    interface Driver extends SimpleBeanEditorDriver<FirebaseApplicationConfiguration, FirebaseApplicationConfigurationEditorView> {}

    interface FirebaseApplicationConfigurationEditorViewUiBinder extends UiBinder<Panel, FirebaseApplicationConfigurationEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    FormGroup projectIdFormGroup;

    @UiField
    FormGroup serviceAccountCredentialsFormGroup;

    @Ignore
    @UiField
    Breadcrumbs breadcrumbs;

    @UiField
    @Path("serviceAccountCredentials")
    TextArea serviceAccountCredentialsTextArea;

    @Ignore
    @UiField
    Label serviceAccountCredentialsWarningLabel;

    @UiField
    @Path("projectId")
    TextBox projectIdTextBox;

    @Ignore
    @UiField
    Label projectIdWarningLabel;

    @UiField
    Button create;

    @Inject
    private Validator validator;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private FirebaseApplicationConfigurationClient firebaseApplicationConfigurationClient;

    @Inject
    private FirebaseApplicationConfigurationEditorView.Driver driver;

    @Inject
    private PlaceManager placeManager;

    private Consumer<FirebaseApplicationConfiguration> submitter = configuration -> {
        Notify.notify("No application specified.");
    };

    @Inject
    public FirebaseApplicationConfigurationEditorView(final FirebaseApplicationConfigurationEditorView.FirebaseApplicationConfigurationEditorViewUiBinder firebaseApplicationConfigurationEditorViewUiBinder) {
        initWidget(firebaseApplicationConfigurationEditorViewUiBinder.createAndBindUi(this));
    }

    public void lockOut() {
        create.setEnabled(false);
        projectIdTextBox.setEnabled(false);
        serviceAccountCredentialsTextArea.setEnabled(false);
    }

    public void unlock() {
        create.setEnabled(true);
        projectIdTextBox.setEnabled(true);
        serviceAccountCredentialsTextArea.setEnabled(true);
    }

    @Override
    public void reset() {

        final Widget root = breadcrumbs.getWidget(0);
        breadcrumbs.clear();
        breadcrumbs.add(root);

        projectIdTextBox.setEnabled(false);
        projectIdWarningLabel.setVisible(false);
        projectIdFormGroup.setValidationState(ValidationState.NONE);

        serviceAccountCredentialsTextArea.setEnabled(false);
        serviceAccountCredentialsWarningLabel.setVisible(false);
        serviceAccountCredentialsFormGroup.setValidationState(ValidationState.NONE);

        create.setVisible(false);

    }

    @Override
    public void createEmpty() {

        submitter = configuration -> {
            Notify.notify("No application specified. Cannot Create.");
        };

        create.setVisible(false);

    }

    @Override
    public void createApplicationConfiguration(final String applicationNameOrId) {

        lockOut();
        loadApplication(applicationNameOrId, application -> {

            reset();
            unlock();

            final FirebaseApplicationConfiguration firebaseApplicationConfiguration;
            firebaseApplicationConfiguration = new FirebaseApplicationConfiguration();
            firebaseApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(firebaseApplicationConfiguration);

            submitter = configuration -> {
                lockOut();
                createNewConfiguration(application.getId(), configuration);
            };

        });

    }

    private void createNewConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {
        firebaseApplicationConfigurationClient.createApplicationConfiguration(
                applicationNameOrId,
                firebaseApplicationConfiguration, new MethodCallback<FirebaseApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable throwable) {
                        unlock();
                        errorModal.setErrorMessage("There was a problem creating the application configuration.");
                        errorModal.show();
                    }

                    @Override
                    public void onSuccess(Method method, FirebaseApplicationConfiguration response) {
                        unlock();
                        Notify.notify("Successfully created Firebase Configuration: " + firebaseApplicationConfiguration.getUniqueIdentifier());
                        editApplicationConfiguration(applicationNameOrId, response);
                    }

                });
    }

    @Override
    public void editApplicationConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        lockOut();
        loadApplication(applicationNameOrId, application -> {

            reset();
            unlock();

            firebaseApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(firebaseApplicationConfiguration);

            submitter = c -> {
                lockOut();
                updateConfiguration(applicationNameOrId, c);
            };

        });

    }

    private void updateConfiguration(final String applicationNameOrId,
                                     final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {
        firebaseApplicationConfigurationClient.updateApplicationConfiguration(
                applicationNameOrId,
                firebaseApplicationConfiguration.getId(),
                firebaseApplicationConfiguration,
                new MethodCallback<FirebaseApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable throwable) {
                        unlock();
                        errorModal.setErrorMessage("There was a problem updating the configuration.");
                        errorModal.show();
                    }

                    @Override
                    public void onSuccess(Method method, FirebaseApplicationConfiguration response) {
                        Notify.notify("Successfully updated: " + firebaseApplicationConfiguration.getUniqueIdentifier());
                        editApplicationConfiguration(applicationNameOrId, response);
                    }

                });
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final FirebaseApplicationConfiguration firebaseApplicationConfiguration = driver.flush();

        boolean failed = false;

        firebaseApplicationConfiguration.setCategory(FIREBASE);

        if (!validator.validateProperty(firebaseApplicationConfiguration, "serviceAccountCredentials").isEmpty()) {
            failed = true;
            serviceAccountCredentialsWarningLabel.setVisible(true);
            serviceAccountCredentialsFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            serviceAccountCredentialsWarningLabel.setVisible(false);
            serviceAccountCredentialsFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(firebaseApplicationConfiguration, "projectId").isEmpty()) {
            failed = true;
            projectIdWarningLabel.setVisible(true);
            projectIdFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            projectIdWarningLabel.setVisible(false);
            projectIdFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!failed) {
            submitter.accept(firebaseApplicationConfiguration);
        }

    }

    private void loadApplication(final String applicationNameOrId, final Consumer<Application> succeeded) {
        applicationClient.getApplication(applicationNameOrId, new MethodCallback<Application>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                Notify.notify("Application not found.");
                errorModal.show();
                createEmpty();
                lockOut();
            }

            @Override
            public void onSuccess(Method method, Application response) {
                succeeded.accept(response);
                reconstructBreadcrumbsWithApplication(response);
            }
        });
    }

    private void reconstructBreadcrumbsWithApplication(final Application application) {

        final Widget root = breadcrumbs.getWidget(0);
        breadcrumbs.clear();
        breadcrumbs.add(root);

        final AnchorListItem applicationLink = new AnchorListItem();
        applicationLink.addClickHandler(e -> {

            final PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.APPLICATION_EDIT)
                .with(ApplicationEditorPresenter.Param.application_id.name(), application.getId())
                .build();

            placeManager.revealPlace(placeRequest);

        });

        applicationLink.setText(application.getName());
        breadcrumbs.add(applicationLink);

    }

}
