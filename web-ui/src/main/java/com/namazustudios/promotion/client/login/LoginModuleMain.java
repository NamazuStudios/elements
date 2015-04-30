package com.namazustudios.promotion.client.login;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.namazustudios.promotion.client.guice.RestyModule;
import com.namazustudios.promotion.client.place.NameTokens;

/**
 * Created by patricktwohig on 4/28/15.
 */
public class LoginModuleMain extends AbstractGinModule {

    @Override
    protected void configure() {

        install(new DefaultModule());
        install(new LoginPresenterModule());
        install(new RestyModule());

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.LOGIN);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.LOGIN);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.LOGIN);

    }

}
