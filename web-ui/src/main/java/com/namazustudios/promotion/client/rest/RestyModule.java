package com.namazustudios.promotion.client.rest;

import com.google.gwt.inject.client.AbstractGinModule;
import com.namazustudios.promotion.model.User;
import org.fusesource.restygwt.client.Defaults;

import javax.inject.Singleton;

/**
 * Created by patricktwohig on 4/16/15.
 */
public class RestyModule extends AbstractGinModule {

    @Override
    protected void configure() {
        binder().bind(UserClient.class);
        binder().bind(LoginClient.class);
        binder().bind(User.class).toProvider(UserProvider.class);
        binder().bind(LoginService.class).to(RestyLoginService.class);
    }

}
