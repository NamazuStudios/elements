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

        final FlowPanel flowPanel = new FlowPanel();

        final Label headerLabel = new Label();
        headerLabel.setText("Welcome!");
        headerLabel.setSize("250px", "1.2em");
        headerLabel.setHorizontalAlignment(HasAutoHorizontalAlignment.ALIGN_CENTER);
        flowPanel.add(headerLabel);

        flowPanel.add(new HTML("<br>"));

        final LoginPanel loginPanel = loginWidgetGinjector.getLoginPanel();
        loginPanel.setSize("250px", "100px");
        flowPanel.add(loginPanel);

        RootLayoutPanel.get().add(flowPanel);

    }

}
