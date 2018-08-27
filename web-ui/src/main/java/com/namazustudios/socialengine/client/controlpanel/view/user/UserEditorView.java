package com.namazustudios.socialengine.client.controlpanel.view.user;

import com.google.common.base.Strings;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.rest.client.internal.UserClient;
import com.namazustudios.socialengine.client.widget.UserLevelEnumDropDown;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.Objects;
import java.util.function.BiConsumer;

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

    private BiConsumer<User, String> submitter = (user, password) -> {
        lockOut();
        updateUser(user, password);
    };

    @Inject
    public UserEditorView(final UserEditorViewUiBinder userEditorViewUiBinder) {
        initWidget(userEditorViewUiBinder.createAndBindUi(this));
    }

    public void lockOut() {

        usernameTextBox.setEnabled(false);
        emailTextBox.setEnabled(false);
        passwordInput.setEnabled(false);
        passwordConfirmInput.setEnabled(false);
        levelDropdown.setEnabled(false);

    }

    public void unlock() {

        usernameTextBox.setEnabled(true);
        emailTextBox.setEnabled(true);
        passwordInput.setEnabled(true);
        passwordConfirmInput.setEnabled(true);
        levelDropdown.setEnabled(true);

    }

    @Override
    public void reset() {

        usernameTextBox.setEnabled(true);
        emailTextBox.setEnabled(true);
        passwordInput.setEnabled(true);
        passwordConfirmInput.setEnabled(true);
        levelDropdown.setEnabled(true);

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

        reset();

        driver.initialize(this);
        driver.edit(new User());

        submitter = (user, password) -> {
            lockOut();
            createNewUser(user, password);
        };

    }

    private void createNewUser(final User user, final String password) {
        userClient.createNewUser(user, password, new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                unlock();
                errorModal.setErrorMessage("There was a problem creating the user.");
                errorModal.show();
            }

            @Override
            public void onSuccess(Method method, User user) {
                unlock();
                Notify.notify("Successfully created user.");
                editUser(user);
            }

        });
    }

    @Override
    public void editUser(final User user) {

        reset();

        driver.initialize(this);
        driver.edit(user);

        submitter = (u, password) -> {
            lockOut();
            updateUser(u, password);
        };

    }

    private void updateUser(final User user, final String password) {
        userClient.updateUser(user.getId(), password, user, new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                unlock();
                errorModal.setErrorMessage("There was a problem updating the user.");
            }

            @Override
            public void onSuccess(Method method, User user) {
                unlock();
                Notify.notify("Successfully updated user.");
                editUser(user);
            }

        });
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

            if(!Objects.equals(password, passwordConfirmInput.getText())) {
                failed = true;
                passwordConfirmWarningLabel.setVisible(true);
                passwordFormGroup.setValidationState(ValidationState.ERROR);
            } else {
                passwordWarningLabel.setVisible(false);
                passwordConfirmWarningLabel.setVisible(false);
                passwordFormGroup.setValidationState(ValidationState.NONE);
            }

        } else {
            passwordWarningLabel.setVisible(false);
            passwordFormGroup.setValidationState(ValidationState.NONE);
        }

        if (!failed) {
            submitter.accept(user, password);
        }

    }

}
