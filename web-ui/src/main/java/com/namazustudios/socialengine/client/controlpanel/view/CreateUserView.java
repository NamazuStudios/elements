package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.base.Strings;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.client.widget.UserLevelEnumDropDown;
import com.namazustudios.socialengine.model.User;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Objects;
import java.util.Set;

/**
 * Created by patricktwohig on 5/5/15.
 */
public class CreateUserView extends ViewImpl implements CreateUserPresenter.MyView, Editor<User> {

    interface Driver extends SimpleBeanEditorDriver<User, CreateUserView> {}

    interface CreateUserViewUiBinder extends UiBinder<Panel, CreateUserView> {}

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
    public CreateUserView(final CreateUserViewUiBinder createUserViewUiBinder) {
        initWidget(createUserViewUiBinder.createAndBindUi(this));
    }

    @Override
    protected void onAttach() {

        super.onAttach();

        usernameWarningLabel.setVisible(false);
        emailWarningLabel.setVisible(false);
        levelWarningLabel.setVisible(false);
        passwordWarningLabel.setVisible(false);
        passwordConfirmWarningLabel.setVisible(false);

        driver.initialize(this);
        driver.edit(new User());

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

        Window.alert("Validation failed: " + failed);

    }



}
