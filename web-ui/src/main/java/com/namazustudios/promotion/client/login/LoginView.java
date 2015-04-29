package com.namazustudios.promotion.client.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/21/15.
 */
public class LoginView extends ViewImpl implements LoginViewPresenter.MyView {

    interface LoginViewUiBinder extends UiBinder<Panel, LoginView> {}

    private static LoginViewUiBinder BINDER = GWT.create(LoginViewUiBinder.class);

//    @UiField
//    protected Input password;
//
//    @UiField
//    protected TextBox username;

    @Inject
    public LoginView(LoginViewUiBinder binder) {
        initWidget(binder.createAndBindUi(this));
    }

}
