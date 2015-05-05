package com.namazustudios.promotion.client.view;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.promotion.client.modal.ErrorModal;
import com.namazustudios.promotion.client.place.NameTokens;
import com.namazustudios.promotion.client.rest.LoginService;
import com.namazustudios.promotion.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/21/15.
 */
public class LoginView extends ViewImpl implements LoginViewPresenter.MyView {

    interface LoginViewUiBinder extends UiBinder<Panel, LoginView> {}

    @UiField
    Input password;

    @UiField
    FormGroup passwordFormGroup;

    @UiField
    TextBox username;

    @UiField
    FormGroup usernameFormGroup;

    @UiField
    ErrorModal errorModal;

    @UiField
    Button login;

    @Inject
    private LoginService loginService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    public LoginView(LoginViewUiBinder binder) {
        initWidget(binder.createAndBindUi(this));
    }

    @UiHandler("login")
    void onClickLogin(final ClickEvent ev) {
        login();
    }

    @UiHandler("password")
    void onKeyPress(KeyPressEvent ev) {
        if (KeyCodes.KEY_ENTER == ev.getCharCode()) {
            login();
        }
    }

    private void login() {

        final String uid = username.getText();
        final String pw = password.getText();

        if (Strings.isNullOrEmpty(uid)) {
            usernameFormGroup.setValidationState(ValidationState.ERROR);
            return;
        } else {
            usernameFormGroup.setValidationState(ValidationState.SUCCESS);
        }

        if (Strings.isNullOrEmpty(pw)) {
            passwordFormGroup.setValidationState(ValidationState.ERROR);
            return;
        } else {
            usernameFormGroup.setValidationState(ValidationState.SUCCESS);
        }

        username.setEnabled(false);
        password.setEnabled(false);
        login.setEnabled(false);

        loginService.login(uid, pw, new MethodCallback<User>() {

            private void finish() {
                username.setEnabled(true);
                password.setEnabled(true);
                login.setEnabled(true);
            }

            @Override
            public void onFailure(Method method, Throwable throwable) {

                finish();

                if (throwable != null) {
                    errorModal.setMessageWithThrowable(throwable);
                } else {
                    errorModal.setErrorMessage("Login failed.  Please check your username/password and try again.");
                }

                errorModal.show();

            }

            @Override
            public void onSuccess(Method method, User user) {
                finish();
                revealControlPanel(user);
            }

        });
    }


    private void revealControlPanel(User user) {

        final PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.CONTROL_PANEL)
                .build();

        placeManager.revealPlace(placeRequest);

    }

}
