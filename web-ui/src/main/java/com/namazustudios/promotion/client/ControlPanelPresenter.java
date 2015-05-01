package com.namazustudios.promotion.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.namazustudios.promotion.client.view.LoginView;
import com.namazustudios.promotion.client.view.LoginViewPresenter;

/**
 * Created by patricktwohig on 4/28/15.
 */
public class ControlPanelPresenter extends AbstractPresenterModule {

    @Override
    protected void configure() {

        bindPresenter(
                LoginViewPresenter.class,
                LoginViewPresenter.MyView.class,
                LoginView.class,
                LoginViewPresenter.MyProxy.class);

    }

}
