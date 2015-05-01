package com.namazustudios.promotion.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.promotion.client.rest.Client;
import com.namazustudios.promotion.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/21/15.
 */
public class LoginView extends ViewImpl implements LoginViewPresenter.MyView {

    interface LoginViewUiBinder extends UiBinder<Panel, LoginView> {}

    @UiField
    Input password;

    @UiField
    TextBox username;

    @Inject
    private Client client;

    @Inject
    public LoginView(LoginViewUiBinder binder) {
        initWidget(binder.createAndBindUi(this));
    }

    @UiHandler("login")
    void onClickLogin(final ClickEvent ev) {
        final String uid = username.getText();
        final String pw = password.getText();

        client.login(uid, pw, new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                Window.alert("Failed!" + throwable.getMessage());
            }

            @Override
            public void onSuccess(Method method, User user) {
                Window.alert("Success!");
            }

        });
    }

}
