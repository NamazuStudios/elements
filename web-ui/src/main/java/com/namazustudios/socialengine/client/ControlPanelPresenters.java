package com.namazustudios.socialengine.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.namazustudios.socialengine.client.view.ControlPanelPresenter;
import com.namazustudios.socialengine.client.view.ControlPanelView;
import com.namazustudios.socialengine.client.view.LoginView;
import com.namazustudios.socialengine.client.view.LoginViewPresenter;

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
