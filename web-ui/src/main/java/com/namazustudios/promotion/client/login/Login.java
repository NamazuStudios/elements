package com.namazustudios.promotion.client.login;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

/**
 * Created by patricktwohig on 3/18/15.
 */
public class Login implements EntryPoint {

    private final LoginWidgetGinjector loginWidgetGinjector = GWT.create(LoginWidgetGinjector.class);

    @Override
    public void onModuleLoad() {

        final LayoutPanel rootLayoutPanel = RootLayoutPanel.get();
        final LoginPanel loginPanel = loginWidgetGinjector.getLoginPanel();

        loginPanel.setSize("250px", "100px");

        rootLayoutPanel.add(loginPanel);
        rootLayoutPanel.setWidgetLeftRight(loginPanel, 20, Style.Unit.PCT, 50, Style.Unit.PCT);
        rootLayoutPanel.setWidgetTopBottom(loginPanel, 5, Style.Unit.EM, 5, Style.Unit.EM);


    }

}
