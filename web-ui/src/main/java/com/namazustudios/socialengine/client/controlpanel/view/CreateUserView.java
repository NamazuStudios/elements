package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.model.User;
import org.gwtbootstrap3.client.ui.*;

import javax.inject.Inject;

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
    @Path("name")
    TextBox emailTextBox;

    @UiField
    Input passwordInput;

    @UiField
    FormGroup passwordFormGroup;

    @UiField
    Input passwordConfirmInput;

    @UiField
    FormGroup passwordConfirmFormGroup;

//    @UiField
//    Anchor levelAnchor;
//
//    @UiField
//    AnchorListItem unprivilegedAnchor;
//
//    @UiField
//    AnchorListItem normalAnchor;
//
//    @UiField
//    AnchorListItem superUserAnchor;
//
//    private User user = new User();
//

    @Inject
    public CreateUserView(final CreateUserViewUiBinder createUserViewUiBinder) {
        initWidget(createUserViewUiBinder.createAndBindUi(this));
    }

//
//    @UiHandler("unprivilegedAnchor")
//    void onClickUnprivileged(ClickEvent clickEvent) {
//        levelAnchor.setText(unprivilegedAnchor.getText());
//        user.setLevel(User.Level.UNPRIVILEGED);
//    }
//
//    @UiHandler("normalAnchor")
//    void onClickNormal(ClickEvent clickEvent) {
//        levelAnchor.setText(normalAnchor.getText());
//        user.setLevel(User.Level.USER);
//    }
//
//    @UiHandler("superUserAnchor")
//    void onClickSuperUser(ClickEvent clickEvent) {
//        levelAnchor.setText(superUserAnchor.getText());
//        user.setLevel(User.Level.SUPERUSER);
//    }

}
