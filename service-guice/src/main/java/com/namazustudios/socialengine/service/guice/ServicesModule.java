package com.namazustudios.socialengine.service.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scope;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.security.SecureRandomPasswordGenerator;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.application.*;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.service.auth.AuthServiceProvider;
import com.namazustudios.socialengine.service.auth.FacebookAuthServiceProvider;
import com.namazustudios.socialengine.service.auth.MockSessionServiceProvider;
import com.namazustudios.socialengine.service.friend.FacebookFriendServiceProvider;
import com.namazustudios.socialengine.service.friend.FriendServiceProvider;
import com.namazustudios.socialengine.service.gameon.*;
import com.namazustudios.socialengine.service.goods.ItemServiceProvider;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemService;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.LeaderboardServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.RankServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.ScoreServiceProvider;
import com.namazustudios.socialengine.service.manifest.ManifestServiceProvider;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.match.StandardMatchServiceUtils;
import com.namazustudios.socialengine.service.mission.MissionService;
import com.namazustudios.socialengine.service.mission.MissionServiceProvider;
import com.namazustudios.socialengine.service.notification.FCMRegistrationServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
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

import javax.inject.Provider;

/**
 * Configures all of the services, using a {@link Scope} for {@link User}, {@link Profile} injections.
 */
public class ServicesModule extends PrivateModule {

    private final Scope scope;

    private final Class<? extends Provider<Attributes>> attributesProvider;

    /**
     * Configures all services to use the following {@link Scope} and {@link Attributes} {@link Provider<Attributes>}
     * type, which is used to match the {@link Attributes} to the associated {@link Scope}.
     *
     * @param scope the scope
     * @param attributesProvider the attributes provider type
     */
    public ServicesModule(final Scope scope,
                          final Class<? extends Provider<Attributes>> attributesProvider) {
        this.scope = scope;
        this.attributesProvider = attributesProvider;
    }

    @Override
    protected void configure() {

        bind(Mapper.class)
                .toProvider(ServicesDozerMapperProvider.class)
                .asEagerSingleton();

        bind(UsernamePasswordAuthService.class)
                .toProvider(AuthServiceProvider.class)
                .in(scope);

        bind(SocialCampaignService.class)
                .toProvider(SocialCampaignServiceProvider.class)
                .in(scope);

        bind(UserService.class)
                .toProvider(UserServiceProvider.class)
                .in(scope);

        bind(ShortLinkService.class)
                .toProvider(ShortLinkServiceProvider.class)
                .in(scope);

        bind(ApplicationService.class)
                .toProvider(ApplicationServiceProvider.class)
                .in(scope);

        bind(ApplicationConfigurationService.class)
                .toProvider(ApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(PSNApplicationConfigurationService.class)
                .toProvider(PSNApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(FacebookApplicationConfigurationService.class)
                .toProvider(FacebookApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(ProfileService.class)
                .toProvider(ProfileServiceProvider.class)
                .in(scope);

        bind(MatchService.class)
                .toProvider(MatchServiceProvider.class)
                .in(scope);

        bind(ManifestService.class)
                .toProvider(ManifestServiceProvider.class)
                .in(scope);

        bind(IosApplicationConfigurationService.class)
                .toProvider(IosApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(GooglePlayApplicationConfigurationService.class)
                .toProvider(GooglePlayApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(MatchmakingApplicationConfigurationService.class)
                .toProvider(MatchmakingConfigurationServiceProvider.class)
                .in(scope);

        bind(FirebaseApplicationConfigurationService.class)
                .toProvider(FirebaseApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(FCMRegistrationService.class)
                .toProvider(FCMRegistrationServiceProvider.class)
                .in(scope);

        bind(ScoreService.class)
                .toProvider(ScoreServiceProvider.class)
                .in(scope);

        bind(RankService.class)
                .toProvider(RankServiceProvider.class)
                .in(scope);

        bind(LeaderboardService.class)
                .toProvider(LeaderboardServiceProvider.class)
                .in(scope);

        bind(FriendService.class)
                .toProvider(FriendServiceProvider.class)
                .in(scope);

        bind(FriendService.class)
                .toProvider(FriendServiceProvider.class)
                .in(scope);

        bind(FacebookFriendService.class)
                .toProvider(FacebookFriendServiceProvider.class)
                .in(scope);

        bind(MockSessionService.class)
                .toProvider(MockSessionServiceProvider.class)
                .in(scope);

        bind(GameOnApplicationConfigurationService.class)
                .toProvider(GameOnApplicationConfigurationServiceProvider.class)
                .in(scope);

        bind(GameOnRegistrationService.class)
                .toProvider(GameOnRegistrationServiceProvider.class)
                .in(scope);

        bind(GameOnSessionService.class)
                .toProvider(GameOnSessionServiceProvider.class)
                .in(scope);

        bind(GameOnTournamentService.class)
                .toProvider(GameOnTournamentServiceProvider.class)
                .in(scope);

        bind(GameOnPlayerTournamentService.class)
                .toProvider(GameOnPlayerTournamentServiceProvider.class)
                .in(scope);

        bind(GameOnMatchService.class)
                .toProvider(GameOnMatchServiceProvider.class)
                .in(scope);

        bind(GameOnAdminPrizeService.class)
                .toProvider(GameOnAdminPrizeServiceProvider.class)
                .in(scope);

        bind(GameOnGamePrizeService.class)
                .toProvider(GameOnGamePrizeServiceProvider.class)
                .in(scope);

        bind(ItemService.class)
                .toProvider(ItemServiceProvider.class)
                .in(scope);

        bind(SimpleInventoryItemService.class)
                .toProvider(SimpleInventoryItemServiceProvider.class)
                .in(scope);

        bind(MissionService.class)
                .toProvider(MissionServiceProvider.class)
                .in(scope);

        bind(ProgressService.class)
                .toProvider(ProgressServiceProvider.class)
                .in(scope);

        bind(FacebookAuthService.class)
                .toProvider(FacebookAuthServiceProvider.class)
                .in(scope);

        bind(RewardIssuanceService.class)
                .toProvider(RewardIssuanceServiceProvider.class)
                .in(scope);

        bind(AppleIapReceiptService.class)
                .toProvider(AppleIapReceiptServiceProvider.class)
                .in(scope);

        bind(GooglePlayIapReceiptService.class)
                .toProvider(GooglePlayIapReceiptServiceProvider.class)
                .in(scope);

        bind(ProfileOverrideService.class)
                .toProvider(ProfileOverrideServiceProvider.class)
                .in(scope);

        bind(SessionService.class).to(DefaultSessionService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();

        bind(Attributes.class).toProvider(attributesProvider);
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
        expose(MatchmakingApplicationConfigurationService.class);
        expose(FirebaseApplicationConfigurationService.class);
        expose(IosApplicationConfigurationService.class);
        expose(GooglePlayApplicationConfigurationService.class);
        expose(ProfileService.class);
        expose(ProfileOverrideService.class);
        expose(MatchService.class);
        expose(ManifestService.class);
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
