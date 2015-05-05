package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.rest.provider.UserProvider;
import com.namazustudios.socialengine.service.AuthService;
import com.namazustudios.socialengine.service.SocialCampaignService;
import com.namazustudios.socialengine.service.UserService;
import com.namazustudios.socialengine.service.auth.AuthServiceProvider;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.user.UserServiceProvider;

import javax.servlet.Servlet;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(User.class)
                .toProvider(UserProvider.class);

        bind(AuthService.class)
                .toProvider(AuthServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(SocialCampaignService.class)
                .toProvider(SocialCampaignServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(UserService.class)
                .toProvider(UserServiceProvider.class)
                .in(ServletScopes.REQUEST);

    }

}
