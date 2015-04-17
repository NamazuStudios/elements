package com.namazustudios.promotion.client.login;

import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;
import com.namazustudios.promotion.client.rest.Client;

import javax.inject.Inject;
import javax.management.openmbean.SimpleType;

/**
 * Created by patricktwohig on 4/16/15.
 */
public class LoginPanel extends LayoutPanel {

    @Inject
    private Client client;

    private final TextBox userIdTextBox;

    private final PasswordTextBox passwordTextBox;

    private final Button loginButton;

    public LoginPanel() {

        final Label userIdLabel = new Label("User ID");
        add(userIdLabel);

        userIdTextBox = new TextBox();
        add(userIdTextBox);

        final Label passwordLabel = new Label("Password");
        add(passwordLabel);

        passwordTextBox = new PasswordTextBox();
        add(passwordTextBox);

        loginButton = new Button();
        add(loginButton);

        setWidgetLeftRight(userIdLabel, 5, Style.Unit.PCT, 95, Style.Unit.PCT);
        setWidgetLeftRight(userIdTextBox, 5, Style.Unit.PCT, 95, Style.Unit.PCT);
        setWidgetLeftRight(passwordLabel, 5, Style.Unit.PCT, 95, Style.Unit.PCT);
        setWidgetLeftRight(passwordTextBox, 5, Style.Unit.PCT, 95, Style.Unit.PCT);
        setWidgetLeftRight(loginButton, 10, Style.Unit.PCT, 35, Style.Unit.PCT);

        double em = 0;



        setWidgetTopHeight(userIdLabel, 0.5, Style.Unit.EM, 1.5, Style.Unit.EM);
        setWidgetTopHeight(userIdTextBox, 2.5, Style.Unit.EM, 1.5, Style.Unit.EM);

    }

}
