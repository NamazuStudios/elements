package com.namazustudios.socialengine.rest.guice;

import com.google.inject.PrivateModule;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.security.SecureRandomPasswordGenerator;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.application.*;
import com.namazustudios.socialengine.service.auth.*;
import com.namazustudios.socialengine.service.friend.FacebookFriendServiceProvider;
import com.namazustudios.socialengine.service.gameon.*;
import com.namazustudios.socialengine.service.goods.ItemServiceProvider;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemService;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.LeaderboardServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.RankServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.ScoreServiceProvider;
import com.namazustudios.socialengine.service.friend.FriendServiceProvider;
import com.namazustudios.socialengine.service.manifest.ManifestServiceProvider;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.match.StandardMatchServiceUtils;
import com.namazustudios.socialengine.service.mission.*;
import com.namazustudios.socialengine.service.notification.FCMRegistrationServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileServiceProvider;
import com.namazustudios.socialengine.service.progress.ProgressService;
import com.namazustudios.socialengine.service.progress.ProgressServiceProvider;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceService;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceServiceProvider;
import com.namazustudios.socialengine.service.shortlink.ShortLinkServiceProvider;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.user.UserServiceProvider;
import com.namazustudios.socialengine.util.DisplayNameGenerator;
import com.namazustudios.socialengine.util.SimpleDisplayNameGenerator;
import org.dozer.Mapper;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class ServicesModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(Mapper.class)
                .toProvider(ServicesDozerMapperProvider.class)
                .asEagerSingleton();

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

        bind(FirebaseApplicationConfigurationService.class)
                .toProvider(FirebaseApplicationConfigurationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FCMRegistrationService.class)
                .toProvider(FCMRegistrationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ScoreService.class)
                .toProvider(ScoreServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(RankService.class)
                .toProvider(RankServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(LeaderboardService.class)
                .toProvider(LeaderboardServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FriendService.class)
                .toProvider(FriendServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FriendService.class)
                .toProvider(FriendServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FacebookFriendService.class)
                .toProvider(FacebookFriendServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(MockSessionService.class)
                .toProvider(MockSessionServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnApplicationConfigurationService.class)
                .toProvider(GameOnApplicationConfigurationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnRegistrationService.class)
                .toProvider(GameOnRegistrationServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnSessionService.class)
                .toProvider(GameOnSessionServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnTournamentService.class)
                .toProvider(GameOnTournamentServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnPlayerTournamentService.class)
                .toProvider(GameOnPlayerTournamentServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnMatchService.class)
                .toProvider(GameOnMatchServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnAdminPrizeService.class)
                .toProvider(GameOnAdminPrizeServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GameOnGamePrizeService.class)
                .toProvider(GameOnGamePrizeServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ItemService.class)
                .toProvider(ItemServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(SimpleInventoryItemService.class)
                .toProvider(SimpleInventoryItemServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(MissionService.class)
                .toProvider(MissionServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(ProgressService.class)
                .toProvider(ProgressServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(FacebookAuthService.class)
                .toProvider(FacebookAuthServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(RewardIssuanceService.class)
                .toProvider(RewardIssuanceServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(AppleIapReceiptService.class)
                .toProvider(AppleIapReceiptServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(GooglePlayIapReceiptService.class)
                .toProvider(GooglePlayIapReceiptServiceProvider.class)
                .in(ServletScopes.REQUEST);

        bind(SessionService.class).to(AnonSessionService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();

        bind(Attributes.class).toProvider(AttributesProvider.class);
        bind(MatchServiceUtils.class).to(StandardMatchServiceUtils.class);

        bind(PasswordGenerator.class).to(SecureRandomPasswordGenerator.class).asEagerSingleton();
        bind(DisplayNameGenerator.class).to(SimpleDisplayNameGenerator.class).asEagerSingleton();

        expose(UsernamePasswordAuthService.class);
        expose(SocialCampaignService.class);
        expose(UserService.class);
        expose(ShortLinkService.class);
        expose(ApplicationService.class);
        expose(ApplicationConfigurationService.class);
        expose(PSNApplicationConfigurationService.class);
        expose(FacebookApplicationConfigurationService.class);
        expose(ProfileService.class);
        expose(MatchService.class);
        expose(ManifestService.class);
        expose(MatchmakingApplicationConfigurationService.class);
        expose(FirebaseApplicationConfigurationService.class);
        expose(FCMRegistrationService.class);
        expose(ScoreService.class);
        expose(RankService.class);
        expose(LeaderboardService.class);
        expose(FriendService.class);
        expose(FacebookFriendService.class);
        expose(MockSessionService.class);
        expose(GameOnApplicationConfigurationService.class);
        expose(GameOnRegistrationService.class);
        expose(GameOnSessionService.class);
        expose(GameOnTournamentService.class);
        expose(GameOnPlayerTournamentService.class);
        expose(GameOnMatchService.class);
        expose(GameOnAdminPrizeService.class);
        expose(GameOnGamePrizeService.class);
        expose(ItemService.class);
        expose(SimpleInventoryItemService.class);
        expose(MissionService.class);
        expose(ProgressService.class);
        expose(FacebookAuthService.class);
        expose(VersionService.class);
        expose(SessionService.class);
        expose(RewardIssuanceService.class);
        expose(AppleIapReceiptService.class);
        expose(GooglePlayIapReceiptService.class);
    }

}
