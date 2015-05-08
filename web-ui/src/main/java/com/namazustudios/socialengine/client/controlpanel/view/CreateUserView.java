package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.widget.UserLevelEnumDropDown;
import com.namazustudios.socialengine.model.User;
import org.gwtbootstrap3.client.ui.*;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created by patricktwohig on 5/5/15.
 */
public class CreateUserView extends ViewImpl implements CreateUserPresenter.MyView, Editor<User> {

    interface CreateUserViewUiBinder extends UiBinder<Panel, CreateUserView> {}

    @UiField
    @Path("name")
    TextBox usernameTextBox;

    @UiField
    FormGroup usernameFormGroup;

    @UiField
    FormGroup emailFormGroup;

    @UiField
    @Path("email")
    TextBox emailTextBox;

    @UiField
    Input passwordInput;

    @UiField
    FormGroup passwordFormGroup;

    @UiField
    Input passwordConfirmInput;

    @UiField
    FormGroup passwordConfirmFormGroup;

    @UiField
    @Path("level")
    UserLevelEnumDropDown levelDropdown;

    @Inject
    private Validator validator;


    @Inject
    public CreateUserView(final CreateUserViewUiBinder createUserViewUiBinder) {
        initWidget(createUserViewUiBinder.createAndBindUi(this));
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final User user = new User();

        String s = "";

        for (ConstraintViolation<User> failure : validator.validate(user)) {
            s += failure.getPropertyPath() + " " + failure.getMessage() + '\n';
        }

        Window.alert("Failures: \n" + s);

    }

}
