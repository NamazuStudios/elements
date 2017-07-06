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
import com.namazustudios.socialengine.client.rest.client.FacebookApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.application.Platform.FACEBOOK;

/**
 * Created by patricktwohig on 6/16/17.
 */
public class FacebookApplicationConfigurationEditorView extends ViewImpl implements
        Editor<FacebookApplicationConfiguration>,
        FacebookApplicationConfigurationEditorPresenter.MyView {

    interface Driver extends SimpleBeanEditorDriver<FacebookApplicationConfiguration, FacebookApplicationConfigurationEditorView> {}

    interface FacebookApplicationConfigurationEditorViewUiBinder extends UiBinder<Panel, FacebookApplicationConfigurationEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    FormGroup applicationIdFormGroup;

    @Ignore
    @UiField
    Breadcrumbs breadcrumbs;

    @UiField
    @Path("applicationId")
    TextBox applicationIdTextBox;

    @Ignore
    @UiField
    Label applicationIdWarningLabel;

    @UiField
    FormGroup applicationSecretFormGroup;

    @UiField
    @Path("applicationSecret")
    TextBox applicationSecretTextBox;

    @Ignore
    @UiField
    Label applicationSecretWarningLabel;

    @UiField
    Button create;

    @Inject
    private Validator validator;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private FacebookApplicationConfigurationClient facebookApplicationConfigurationClient;

    @Inject
    private Driver driver;

    @Inject
    private PlaceManager placeManager;

    private Consumer<FacebookApplicationConfiguration> submitter = configuration -> {
        Notify.notify("No application specified.");
    };

    @Inject
    public FacebookApplicationConfigurationEditorView(final FacebookApplicationConfigurationEditorViewUiBinder facebookApplicationConfigurationEditorViewUiBinder) {
        initWidget(facebookApplicationConfigurationEditorViewUiBinder.createAndBindUi(this));
    }

    public void lockOut() {
        create.setEnabled(false);
        applicationIdTextBox.setEnabled(false);
        applicationSecretTextBox.setEnabled(false);
    }

    public void unlock() {
        create.setEnabled(true);
        applicationIdTextBox.setEnabled(true);
        applicationSecretTextBox.setEnabled(true);
    }

    @Override
    public void reset() {

        final Widget root = breadcrumbs.getWidget(0);
        breadcrumbs.clear();
        breadcrumbs.add(root);

        applicationIdTextBox.setEnabled(false);
        applicationSecretTextBox.setEnabled(false);

        applicationIdWarningLabel.setVisible(false);
        applicationIdFormGroup.setValidationState(ValidationState.NONE);

        applicationSecretWarningLabel.setVisible(false);
        applicationSecretFormGroup.setValidationState(ValidationState.NONE);

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

            final FacebookApplicationConfiguration facebookApplicationConfiguration;
            facebookApplicationConfiguration = new FacebookApplicationConfiguration();
            facebookApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(facebookApplicationConfiguration);

            submitter = configuration -> {
                lockOut();
                createNewConfiguration(application.getId(), configuration);
            };

        });

    }

    private void createNewConfiguration(
            final String applicationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {
        facebookApplicationConfigurationClient.createApplicationConfiguration(
                applicationNameOrId,
                facebookApplicationConfiguration, new MethodCallback<FacebookApplicationConfiguration>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                unlock();
                errorModal.setErrorMessage("There was a problem creating the application configuration.");
                errorModal.show();
            }

            @Override
            public void onSuccess(Method method, FacebookApplicationConfiguration response) {
                unlock();
                Notify.notify("Successfully created Facebook Configuration: " + facebookApplicationConfiguration.getUniqueIdentifier());
                editApplicationConfiguration(applicationNameOrId, response);
            }

        });
    }

    @Override
    public void editApplicationConfiguration(
            final String applicationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        lockOut();
        loadApplication(applicationNameOrId, application -> {

            reset();
            unlock();

            facebookApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(facebookApplicationConfiguration);

            submitter = c -> {
                lockOut();
                updateConfiguration(applicationNameOrId, c);
            };

        });

    }

    private void updateConfiguration(final String applicationNameOrId,
                                     final FacebookApplicationConfiguration facebookApplicationConfiguration) {
        facebookApplicationConfigurationClient.updateApplicationConfiguration(
            applicationNameOrId,
            facebookApplicationConfiguration.getId(),
            facebookApplicationConfiguration,
            new MethodCallback<FacebookApplicationConfiguration>() {

                @Override
                public void onFailure(Method method, Throwable throwable) {
                    unlock();
                    errorModal.setErrorMessage("There was a problem updating the configuration.");
                    errorModal.show();
                }

                @Override
                public void onSuccess(Method method, FacebookApplicationConfiguration response) {
                    Notify.notify("Successfully updated: " + facebookApplicationConfiguration.getUniqueIdentifier());
                    editApplicationConfiguration(applicationNameOrId, response);
                }

            });
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final FacebookApplicationConfiguration facebookApplicationConfiguration = driver.flush();

        boolean failed = false;

        facebookApplicationConfiguration.setPlatform(FACEBOOK);

        if (!validator.validateProperty(facebookApplicationConfiguration, "applicationId").isEmpty()) {
            failed = true;
            applicationIdWarningLabel.setVisible(true);
            applicationIdFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            applicationIdWarningLabel.setVisible(false);
            applicationIdFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(facebookApplicationConfiguration, "applicationSecret").isEmpty()) {
            failed = true;
            applicationIdWarningLabel.setVisible(true);
            applicationIdFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            applicationIdWarningLabel.setVisible(false);
            applicationIdFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!failed) {
            submitter.accept(facebookApplicationConfiguration);
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
