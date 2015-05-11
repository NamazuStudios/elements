package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.base.Strings;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.client.widget.UserLevelEnumDropDown;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.growl.client.ui.Growl;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.Objects;

/**
 * Created by patricktwohig on 5/5/15.
 */
public class UserEditorView extends ViewImpl implements UserEditorPresenter.MyView, Editor<User> {

    interface Driver extends SimpleBeanEditorDriver<User, UserEditorView> {}

    interface UserEditorViewUiBinder extends UiBinder<Panel, UserEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    FormGroup usernameFormGroup;

    @UiField
    @Path("name")
    TextBox usernameTextBox;

    @Ignore
    @UiField
    Label usernameWarningLabel;

    @UiField
    @Path("email")
    TextBox emailTextBox;

    @UiField
    FormGroup emailFormGroup;

    @Ignore
    @UiField
    Label emailWarningLabel;

    @UiField
    FormGroup passwordFormGroup;

    @Ignore
    @UiField
    Input passwordInput;

    @UiField
    FormGroup passwordConfirmFormGroup;

    @Ignore
    @UiField
    Input passwordConfirmInput;

    @Ignore
    @UiField
    Label passwordWarningLabel;

    @Ignore
    @UiField
    Label passwordConfirmWarningLabel;

    @UiField
    FormGroup levelFormGroup;

    @UiField
    @Path("level")
    UserLevelEnumDropDown levelDropdown;

    @Ignore
    @UiField
    Label levelWarningLabel;

    @Inject
    private Validator validator;

    @Inject
    private UserClient userClient;

    @Inject
    private Driver driver;

    @Inject
    private PlaceManager placeManager;

    @Inject
    public UserEditorView(final UserEditorViewUiBinder userEditorViewUiBinder) {
        initWidget(userEditorViewUiBinder.createAndBindUi(this));
    }

    @Override
    public void reset() {

        usernameWarningLabel.setVisible(false);
        usernameFormGroup.setValidationState(ValidationState.NONE);

        emailWarningLabel.setVisible(false);
        emailFormGroup.setValidationState(ValidationState.NONE);

        levelWarningLabel.setVisible(false);
        levelFormGroup.setValidationState(ValidationState.NONE);

        passwordWarningLabel.setVisible(false);
        passwordConfirmWarningLabel.setVisible(false);
        passwordFormGroup.setValidationState(ValidationState.NONE);

    }

    @Override
    public void createUser() {

        usernameWarningLabel.setVisible(false);
        emailWarningLabel.setVisible(false);
        levelWarningLabel.setVisible(false);
        passwordWarningLabel.setVisible(false);
        passwordConfirmWarningLabel.setVisible(false);

        driver.initialize(this);
        driver.edit(new User());

    }

    @Override
    public void editUser(final User user) {
        driver.initialize(this);
        driver.edit(user);
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final User user = driver.flush();

        boolean failed = false;

        if (!validator.validateProperty(user, "name").isEmpty()) {
            failed = true;
            usernameWarningLabel.setVisible(true);
            usernameFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            usernameWarningLabel.setVisible(false);
            usernameFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(user, "email").isEmpty()) {
            failed = true;
            emailWarningLabel.setVisible(true);
            emailFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            emailWarningLabel.setVisible(false);
            emailFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!validator.validateProperty(user, "level").isEmpty()) {
            failed = true;
            levelWarningLabel.setVisible(true);
            levelFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            levelWarningLabel.setVisible(false);
            levelFormGroup.setValidationState(ValidationState.NONE);
        }

        final String password = passwordInput.getText();

        if (Strings.isNullOrEmpty(password)) {
            passwordWarningLabel.setVisible(true);
            passwordFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            passwordWarningLabel.setVisible(false);
            passwordFormGroup.setValidationState(ValidationState.NONE);
        }

        if(!Objects.equals(password, passwordConfirmInput.getText())) {
            failed = true;
            passwordConfirmWarningLabel.setVisible(true);
            passwordFormGroup.setValidationState(ValidationState.ERROR);
        } else {
            passwordWarningLabel.setVisible(false);
            passwordConfirmWarningLabel.setVisible(false);
            passwordFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!failed) {
            userClient.craeteNewUser(user, password, new MethodCallback<User>() {

                @Override
                public void onFailure(Method method, Throwable throwable) {
                    errorModal.setErrorMessage("There was a problem creating the user.");
                }

                @Override
                public void onSuccess(Method method, User user) {

                    Growl.growl("Successfully created user.");

                    final PlaceRequest placeRequest = new PlaceRequest.Builder()
                            .nameToken(NameTokens.MAIN)
                            .build();

                    placeManager.revealPlace(placeRequest);

                }

            });
        }

    }

}
