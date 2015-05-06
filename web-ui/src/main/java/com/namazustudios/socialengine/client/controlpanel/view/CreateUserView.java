package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/5/15.
 */
public class CreateUserView extends ViewImpl implements CreateUserPresenter.MyView {

    interface CreateUserViewUiBinder extends UiBinder<Panel, CreateUserView> {}

    @UiField
    TextBox usernameTextBox;

    @UiField
    FormGroup usernameFormGroup;

    @UiField
    FormGroup emailFormGroup;

    @UiField
    TextBox emailTextBox;

    @UiField
    Input passwordInput;

    @UiField
    FormGroup passwordFormGroup;

    @UiField
    Input passwordConfirmInput;

    @UiField
    FormGroup passwordConfirmFormGroup;

    @Inject
    public CreateUserView(final CreateUserViewUiBinder createUserViewUiBinder) {
        initWidget(createUserViewUiBinder.createAndBindUi(this));
    }

}