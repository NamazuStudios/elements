package com.namazustudios.promotion.client.login;

import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.namazustudios.promotion.client.place.NameTokens;

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
