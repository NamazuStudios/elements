package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.UserProvider;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.application.ApplicationConfigurationServiceProvider;
import com.namazustudios.socialengine.service.application.ApplicationServiceProvider;
import com.namazustudios.socialengine.service.application.FacebookApplicationConfigurationServiceProvider;
import com.namazustudios.socialengine.service.application.PSNApplicationConfigurationServiceProvider;
import com.namazustudios.socialengine.service.auth.AuthServiceProvider;
import com.namazustudios.socialengine.service.auth.StandardFacebookAuthService;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileServiceProvider;
import com.namazustudios.socialengine.rest.provider.HttpRequestAttributeProfileSupplierProvider;
import com.namazustudios.socialengine.service.shortlink.ShortLinkServiceProvider;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.user.UserServiceProvider;

import java.util.function.Supplier;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(User.class)
                .toProvider(UserProvider.class);

        bind(new TypeLiteral<Supplier<Profile>>(){})
                .toProvider(HttpRequestAttributeProfileSupplierProvider.class);

        bind(AuthService.class)
                .toProvider(AuthServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(SocialCampaignService.class)
                .toProvider(SocialCampaignServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(UserService.class)
                .toProvider(UserServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ShortLinkService.class)
                .toProvider(ShortLinkServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ApplicationService.class)
                .toProvider(ApplicationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ApplicationConfigurationService.class)
                .toProvider(ApplicationConfigurationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(PSNApplicationConfigurationService.class)
                .toProvider(PSNApplicationConfigurationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FacebookApplicationConfigurationService.class)
                .toProvider(FacebookApplicationConfigurationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ProfileService.class)
                .toProvider(ProfileServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(MatchService.class)
                .toProvider(MatchServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FacebookAuthService.class).to(StandardFacebookAuthService.class);

    }

}
