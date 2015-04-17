package com.namazustudios.promotion.client.login;

import com.google.gwt.user.client.ui.*;
import com.namazustudios.promotion.client.rest.Client;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/16/15.
 */
public class LoginPanel extends FlowPanel {

    @Inject
    private Client client;

    private final TextBox userIdTextBox;

    private final PasswordTextBox passwordTextBox;

    private final Button loginButton;

    public LoginPanel() {
        Label label;

        label = new Label("User ID");
        label.setWidth("100%");
        label.setHeight("15pt");
        add(label);

        userIdTextBox = new TextBox();
        userIdTextBox.setWidth("100%");
        userIdTextBox.setHeight("15pt");
        add(userIdTextBox);

        label = new Label("Password");
        label.setWidth("100%");
        label.setHeight("15pt");
        add(label);

        passwordTextBox = new PasswordTextBox();
        passwordTextBox.setWidth("100%");
        passwordTextBox.setHeight("15pt");
        add(passwordTextBox);

        loginButton = new Button();
        loginButton.setText("Login");
        loginButton.setWidth("25%");
        loginButton.setHeight("25pt");
        add(loginButton);

    }

}
