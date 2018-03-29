package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.application.*;
import com.namazustudios.socialengine.service.auth.AuthServiceProvider;
import com.namazustudios.socialengine.service.auth.StandardFacebookAuthService;
import com.namazustudios.socialengine.service.auth.AnonSessionService;
import com.namazustudios.socialengine.service.manifest.ManifestServiceProvider;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.notification.FCMRegistrationServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileServiceProvider;
import com.namazustudios.socialengine.service.shortlink.ShortLinkServiceProvider;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.user.UserServiceProvider;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(UsernamePasswordAuthService.class)
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

        bind(ManifestService.class)
                .toProvider(ManifestServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(MatchmakingApplicationConfigurationService.class)
                .toProvider(MatchmakingConfigurationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FCMRegistrationService.class)
                .toProvider(FCMRegistrationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(SessionService.class).to(AnonSessionService.class);
        bind(FacebookAuthService.class).to(StandardFacebookAuthService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();

    }

}
