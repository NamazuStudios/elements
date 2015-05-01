package com.namazustudios.promotion.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.gwtplatform.mvp.client.ViewImpl;
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
    public Input password;

    @UiField
    public TextBox username;

    @Inject
    public LoginView(LoginViewUiBinder binder) {
        initWidget(binder.createAndBindUi(this));
    }

}
