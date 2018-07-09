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
import com.namazustudios.socialengine.client.rest.client.ApplicationClient;
import com.namazustudios.socialengine.client.rest.client.GameOnApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;

public class GameOnApplicationConfigurationEditorView extends ViewImpl implements
        Editor<GameOnApplicationConfiguration>,
        GameOnApplicationConfigurationEditorPresenter.MyView  {

    interface Driver extends SimpleBeanEditorDriver<GameOnApplicationConfiguration, GameOnApplicationConfigurationEditorView> {}

    interface GameOnApplicationConfigurationEditorViewUiBinder extends UiBinder<Panel, GameOnApplicationConfigurationEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    FormGroup gameIdFormGroup;

    @UiField
    FormGroup adminApiKeyFormGroup;

    @UiField
    FormGroup publicApiKeyFormGroup;

    @UiField
    FormGroup publicKeyFormGroup;

    @Editor.Ignore
    @UiField
    Breadcrumbs breadcrumbs;

    @Editor.Ignore
    @UiField
    Label gameIdWarningLabel;

    @Editor.Ignore
    @UiField
    Label publicApiKeyWarningLabel;

    @Editor.Ignore
    @UiField
    Label adminApiKeyWarningLabel;

    @Editor.Ignore
    @UiField
    Label publicKeyWarningLabel;

    @UiField
    @Editor.Path("gameId")
    TextBox gameIdTextBox;

    @UiField
    @Editor.Path("publicApiKey")
    TextBox publicApiKeyTextBox;

    @UiField
    @Editor.Path("adminApiKey")
    TextBox adminApiKeyTextBox;

    @UiField
    @Editor.Path("publicKey")
    TextArea publicKeyTextArea;

    @UiField
    Button create;

    @Inject
    private Validator validator;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private GameOnApplicationConfigurationClient gameOnApplicationConfigurationClient;

    @Inject
    private GameOnApplicationConfigurationEditorView.Driver driver;

    @Inject
    private PlaceManager placeManager;

    private Consumer<GameOnApplicationConfiguration> submitter = configuration -> {
        Notify.notify("No application specified.");
    };

    @Inject
    public GameOnApplicationConfigurationEditorView(final GameOnApplicationConfigurationEditorViewUiBinder gameOnApplicationConfigurationEditorViewUiBinder) {
        initWidget(gameOnApplicationConfigurationEditorViewUiBinder.createAndBindUi(this));
    }

    public void lockOut() {
        create.setEnabled(false);
        gameIdTextBox.setEnabled(false);
        adminApiKeyTextBox.setEnabled(false);
        publicApiKeyTextBox.setEnabled(false);
        publicKeyTextArea.setEnabled(false);
    }

    public void unlock() {
        create.setEnabled(true);
        gameIdTextBox.setEnabled(true);
        adminApiKeyTextBox.setEnabled(true);
        publicApiKeyTextBox.setEnabled(true);
        publicKeyTextArea.setEnabled(true);
    }

    @Override
    public void reset() {

        final Widget root = breadcrumbs.getWidget(0);
        breadcrumbs.clear();
        breadcrumbs.add(root);

        gameIdTextBox.setEnabled(false);
        gameIdWarningLabel.setVisible(false);
        gameIdFormGroup.setValidationState(ValidationState.NONE);

        publicApiKeyTextBox.setEnabled(false);
        publicApiKeyWarningLabel.setVisible(false);
        publicApiKeyFormGroup.setValidationState(ValidationState.NONE);

        adminApiKeyTextBox.setEnabled(false);
        adminApiKeyWarningLabel.setVisible(false);
        adminApiKeyFormGroup.setValidationState(ValidationState.NONE);

        publicKeyTextArea.setEnabled(false);
        publicKeyWarningLabel.setVisible(false);
        publicKeyFormGroup.setValidationState(ValidationState.NONE);

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

            final GameOnApplicationConfiguration gameOnApplicationConfiguration;
            gameOnApplicationConfiguration = new GameOnApplicationConfiguration();
            gameOnApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(gameOnApplicationConfiguration);

            submitter = configuration -> {
                lockOut();
                createNewConfiguration(application.getId(), configuration);
            };

        });

    }

    private void createNewConfiguration(
            final String applicationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        gameOnApplicationConfigurationClient.createApplicationConfiguration(
            applicationNameOrId, gameOnApplicationConfiguration, new MethodCallback<GameOnApplicationConfiguration>() {

                @Override
                public void onFailure(Method method, Throwable throwable) {
                    unlock();
                    errorModal.setErrorMessage("There was a problem creating the application configuration.");
                    errorModal.show();
                }

                @Override
                public void onSuccess(Method method, GameOnApplicationConfiguration response) {
                    unlock();
                    Notify.notify("Successfully created Game On Configuration: " + gameOnApplicationConfiguration.getUniqueIdentifier());
                    editApplicationConfiguration(applicationNameOrId, response);
                }

            });
    }

    @Override
    public void editApplicationConfiguration(
            final String applicationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {

        lockOut();
        loadApplication(applicationNameOrId, application -> {

            reset();
            unlock();

            gameOnApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(gameOnApplicationConfiguration);

            submitter = c -> {
                lockOut();
                updateConfiguration(applicationNameOrId, c);
            };

        });

    }

    private void updateConfiguration(final String applicationNameOrId,
                                     final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        gameOnApplicationConfigurationClient.updateApplicationConfiguration(
                applicationNameOrId,
                gameOnApplicationConfiguration.getId(),
                gameOnApplicationConfiguration,
                new MethodCallback<GameOnApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable throwable) {
                        unlock();
                        errorModal.setErrorMessage("There was a problem updating the configuration.");
                        errorModal.show();
                    }

                    @Override
                    public void onSuccess(Method method, GameOnApplicationConfiguration response) {
                        Notify.notify("Successfully updated: " + gameOnApplicationConfiguration.getUniqueIdentifier());
                        editApplicationConfiguration(applicationNameOrId, response);
                    }

                });
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final GameOnApplicationConfiguration firebaseApplicationConfiguration = driver.flush();

        boolean failed = false;

        firebaseApplicationConfiguration.setCategory(FIREBASE);

        if (!validator.validateProperty(firebaseApplicationConfiguration, "gameId").isEmpty()) {
            failed = true;
            gameIdWarningLabel.setVisible(true);
            gameIdFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            gameIdWarningLabel.setVisible(false);
            gameIdFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(firebaseApplicationConfiguration, "publicApiKey").isEmpty()) {
            failed = true;
            publicApiKeyWarningLabel.setVisible(true);
            publicApiKeyFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            publicApiKeyWarningLabel.setVisible(false);
            publicApiKeyFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(firebaseApplicationConfiguration, "adminApiKey").isEmpty()) {
            failed = true;
            adminApiKeyWarningLabel.setVisible(true);
            adminApiKeyFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            adminApiKeyWarningLabel.setVisible(false);
            adminApiKeyFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(firebaseApplicationConfiguration, "publicKey").isEmpty()) {
            failed = true;
            publicKeyWarningLabel.setVisible(true);
            publicKeyFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            publicKeyWarningLabel.setVisible(false);
            publicKeyFormGroup.setValidationState(ValidationState.NONE);
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