package com.namazustudios.socialengine.client.rest;

import com.google.gwt.inject.client.AbstractGinModule;
import com.namazustudios.socialengine.client.rest.client.*;
import com.namazustudios.socialengine.client.rest.gin.UserProvider;
import com.namazustudios.socialengine.client.rest.service.LoginService;
import com.namazustudios.socialengine.client.rest.service.RestyLoginService;
import com.namazustudios.socialengine.model.User;

/**
 * Created by patricktwohig on 4/16/15.
 */
public class RestyModule extends AbstractGinModule {

    @Override
    protected void configure() {

        binder().bind(UserClient.class);
        binder().bind(LoginClient.class);
        binder().bind(ShortLinkClient.class);

        binder().bind(ApplicationClient.class);
        binder().bind(IosApplicationConfigurationClient.class);
        binder().bind(GooglePlayApplicationConfigurationClient.class);
        binder().bind(PSNApplicationConfigurationClient.class);
        binder().bind(FacebookApplicationConfigurationClient.class);

        binder().bind(User.class).toProvider(UserProvider.class);
        binder().bind(LoginService.class).to(RestyLoginService.class);

    }

}
