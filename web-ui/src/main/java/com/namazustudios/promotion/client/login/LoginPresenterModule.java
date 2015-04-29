package com.namazustudios.promotion.client.login;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by patricktwohig on 4/28/15.
 */
public class LoginPresenterModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(
                LoginViewPresenter.class,
                LoginViewPresenter.MyView.class,
                LoginView.class,
                LoginViewPresenter.MyProxy.class);
    }

}
