package com.namazustudios.socialengine.client.controlpanel;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.namazustudios.socialengine.client.controlpanel.view.ControlPanelPresenter;
import com.namazustudios.socialengine.client.controlpanel.view.ControlPanelView;
import com.namazustudios.socialengine.client.controlpanel.view.LoginView;
import com.namazustudios.socialengine.client.controlpanel.view.LoginViewPresenter;

/**
 * Created by patricktwohig on 4/28/15.
 */
public class ControlPanelPresenters extends AbstractPresenterModule {

    @Override
    protected void configure() {

        bindPresenter(
                LoginViewPresenter.class,
                LoginViewPresenter.MyView.class,
                LoginView.class,
                LoginViewPresenter.MyProxy.class);

        bindPresenter(
                ControlPanelPresenter.class,
                ControlPanelPresenter.MyView.class,
                ControlPanelView.class,
                ControlPanelPresenter.MyProxy.class);

    }

}
