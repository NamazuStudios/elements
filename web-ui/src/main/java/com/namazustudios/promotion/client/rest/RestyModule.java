package com.namazustudios.promotion.client.rest;

import com.google.gwt.inject.client.AbstractGinModule;
import com.namazustudios.promotion.model.User;

import javax.inject.Singleton;

/**
 * Created by patricktwohig on 4/16/15.
 */
public class RestyModule extends AbstractGinModule {

    @Override
    protected void configure() {
        binder().bind(Client.class).toProvider(ClientProvider.class).in(Singleton.class);
        binder().bind(LoginService.class).to(RestyLoginService.class);
        binder().bind(User.class).toProvider(UserProvider.class);
    }

}
