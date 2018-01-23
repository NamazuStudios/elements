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
import com.namazustudios.socialengine.client.rest.client.MatchmakingApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.CallbackDefinition;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FACEBOOK;

/**
 * Created by patricktwohig on 6/16/17.
 */
public class MatchmakingApplicationConfigurationEditorView extends ViewImpl implements
        Editor<MatchmakingApplicationConfiguration>,
        MatchmakingApplicationConfigurationEditorPresenter.MyView {

    interface Driver extends SimpleBeanEditorDriver<MatchmakingApplicationConfiguration, MatchmakingApplicationConfigurationEditorView> {}

    interface MatchmakingApplicationConfigurationEditorViewUiBinder extends UiBinder<Panel, MatchmakingApplicationConfigurationEditorView> {}

    @Ignore
    @UiField
    ErrorModal errorModal;

    @Ignore
    @UiField
    Breadcrumbs breadcrumbs;

    @Ignore
    @UiField
    FormGroup schemeFormGroup;

    @UiField
    @Path("scheme")
    TextBox schemeTextBox;

    @Ignore
    @UiField
    Label schemeWarningLabel;

    @UiField
    @Ignore
    FormGroup successFormGroup;

    @UiField
    @Path("success.module")
    TextBox successModuleTextBox;

    @UiField
    @Path("success.method")
    TextBox successMethodTextBox;

    @Ignore
    @UiField
    Label successModuleWarningLabel;

    @Ignore
    @UiField
    Label successMethodWarningLabel;

    @UiField
    Button create;

    @Inject
    private Validator validator;

    @Inject
    private ApplicationClient applicationClient;

    @Inject
    private MatchmakingApplicationConfigurationClient matchmakingApplicationConfigurationClient;

    @Inject
    private Driver driver;

    @Inject
    private PlaceManager placeManager;

    private Consumer<MatchmakingApplicationConfiguration> submitter = configuration -> {
        Notify.notify("No application specified.");
    };

    @Inject
    public MatchmakingApplicationConfigurationEditorView(final MatchmakingApplicationConfigurationEditorViewUiBinder matchmakingApplicationConfigurationEditorViewUiBinder) {
        initWidget(matchmakingApplicationConfigurationEditorViewUiBinder.createAndBindUi(this));
    }

    public void lockOut() {
        create.setEnabled(false);
        schemeTextBox.setEnabled(false);
        successModuleTextBox.setEnabled(false);
        successMethodTextBox.setEnabled(false);
    }

    public void unlock() {
        create.setEnabled(true);
        schemeTextBox.setEnabled(true);
        successModuleTextBox.setEnabled(true);
        successMethodTextBox.setEnabled(true);
    }

    @Override
    public void reset() {

        final Widget root = breadcrumbs.getWidget(0);
        breadcrumbs.clear();
        breadcrumbs.add(root);

        schemeTextBox.setEnabled(false);
        successMethodTextBox.setEnabled(false);
        successMethodTextBox.setEnabled(false);

        schemeWarningLabel.setVisible(false);
        schemeFormGroup.setValidationState(ValidationState.NONE);

        successModuleWarningLabel.setVisible(false);
        successMethodWarningLabel.setVisible(false);
        successFormGroup.setValidationState(ValidationState.NONE);

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

            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration;
            matchmakingApplicationConfiguration = new MatchmakingApplicationConfiguration();
            matchmakingApplicationConfiguration.setParent(application);
            matchmakingApplicationConfiguration.setAlgorithm(MatchingAlgorithm.FIFO);

            final CallbackDefinition successCallbackDefinition = new CallbackDefinition();
            matchmakingApplicationConfiguration.setSuccess(successCallbackDefinition);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(matchmakingApplicationConfiguration);

            submitter = configuration -> {
                lockOut();
                createNewConfiguration(application.getId(), configuration);
            };

        });

    }

    private void createNewConfiguration(
            final String applicationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        matchmakingApplicationConfigurationClient.createApplicationConfiguration(
                applicationNameOrId,
                matchmakingApplicationConfiguration, new MethodCallback<MatchmakingApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable throwable) {
                        unlock();
                        errorModal.setErrorMessage("There was a problem creating the application configuration.");
                        errorModal.show();
                    }

                    @Override
                    public void onSuccess(Method method, MatchmakingApplicationConfiguration response) {
                        unlock();
                        final String uid = response.getUniqueIdentifier();
                        Notify.notify("Successfully created Matchmaking Configuration: " + uid);
                        editApplicationConfiguration(applicationNameOrId, response);
                    }

                });
    }

    @Override
    public void editApplicationConfiguration(
            final String applicationNameOrId,
            final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {

        lockOut();
        loadApplication(applicationNameOrId, application -> {

            reset();
            unlock();

            matchmakingApplicationConfiguration.setParent(application);

            create.setVisible(true);
            driver.initialize(this);
            driver.edit(matchmakingApplicationConfiguration);

            submitter = c -> {
                lockOut();
                updateConfiguration(applicationNameOrId, c);
            };

        });

    }

    private void updateConfiguration(final String applicationNameOrId,
                                     final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration) {
        matchmakingApplicationConfigurationClient.updateApplicationConfiguration(
                applicationNameOrId,
                matchmakingApplicationConfiguration.getId(),
                matchmakingApplicationConfiguration,
                new MethodCallback<MatchmakingApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable throwable) {
                        unlock();
                        errorModal.setErrorMessage("There was a problem updating the configuration.");
                        errorModal.show();
                    }

                    @Override
                    public void onSuccess(Method method, MatchmakingApplicationConfiguration response) {
                        Notify.notify("Successfully updated: " + matchmakingApplicationConfiguration.getUniqueIdentifier());
                        editApplicationConfiguration(applicationNameOrId, response);
                    }

                });
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final MatchmakingApplicationConfiguration matchmakingApplicationConfiguration = driver.flush();
        final CallbackDefinition successCallbackDefinition = matchmakingApplicationConfiguration.getSuccess();

        boolean failed = false;

        matchmakingApplicationConfiguration.setCategory(FACEBOOK);

        if (!validator.validateProperty(matchmakingApplicationConfiguration, "scheme").isEmpty()) {
            failed = true;
            schemeWarningLabel.setVisible(true);
            schemeFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            schemeWarningLabel.setVisible(false);
            schemeFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(successCallbackDefinition, "module").isEmpty()) {
            failed = true;
            successModuleWarningLabel.setVisible(true);
            successFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            successModuleWarningLabel.setVisible(false);
            successFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(successCallbackDefinition, "method").isEmpty()) {
            failed = true;
            successMethodWarningLabel.setVisible(true);
            successFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            successMethodWarningLabel.setVisible(false);
            successFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!failed) {
            submitter.accept(matchmakingApplicationConfiguration);
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
