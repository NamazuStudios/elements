package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.application.*;
import com.namazustudios.socialengine.service.auth.*;
import com.namazustudios.socialengine.service.blockchain.bsc.*;
import com.namazustudios.socialengine.service.blockchain.evm.EvmSmartContractServiceProvider;
import com.namazustudios.socialengine.service.blockchain.neo.*;
import com.namazustudios.socialengine.service.blockchain.omni.SmartContractServiceProvider;
import com.namazustudios.socialengine.service.blockchain.omni.VaultServiceProvider;
import com.namazustudios.socialengine.service.blockchain.omni.WalletServiceProvider;
import com.namazustudios.socialengine.service.follower.FollowerServiceProvider;
import com.namazustudios.socialengine.service.formidium.FormidiumService;
import com.namazustudios.socialengine.service.formidium.FormidiumServiceProvider;
import com.namazustudios.socialengine.service.friend.FacebookFriendServiceProvider;
import com.namazustudios.socialengine.service.friend.FriendServiceProvider;
import com.namazustudios.socialengine.service.goods.ItemServiceProvider;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.health.DefaultHealthStatusService;
import com.namazustudios.socialengine.service.inventory.*;
import com.namazustudios.socialengine.service.leaderboard.LeaderboardServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.RankServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.ScoreServiceProvider;
import com.namazustudios.socialengine.service.manifest.ManifestServiceProvider;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.mission.MissionService;
import com.namazustudios.socialengine.service.mission.MissionServiceProvider;
import com.namazustudios.socialengine.service.notification.FCMRegistrationServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileServiceProvider;
import com.namazustudios.socialengine.service.progress.ProgressService;
import com.namazustudios.socialengine.service.progress.ProgressServiceProvider;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceService;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceServiceProvider;
import com.namazustudios.socialengine.service.savedata.SaveDataDocumentServiceProvider;
import com.namazustudios.socialengine.service.schema.MetadataSpecService;
import com.namazustudios.socialengine.service.schema.MetadataSpecServiceProvider;
import com.namazustudios.socialengine.service.schema.TokenTemplateService;
import com.namazustudios.socialengine.service.schema.TokenTemplateServiceProvider;
import com.namazustudios.socialengine.service.shortlink.ShortLinkServiceProvider;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.user.UserServiceProvider;

public class ScopedServicesModule extends AbstractModule {

    private final Scope scope;

    public ScopedServicesModule(Scope scope) {
        this.scope = scope;
    }

    @Override
    protected void configure() {

        bind(UsernamePasswordAuthService.class)
                .toProvider(UsernamePasswordAuthServiceProvider.class)
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

        bind(NeoSmartContractService.class)
                .toProvider(NeoSmartContractServiceProvider.class)
                .in(scope);

        bind(BscSmartContractService.class)
                .toProvider(BscSmartContractServiceProvider.class)
                .in(scope);

        bind(FollowerService.class)
                .toProvider(FollowerServiceProvider.class)
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

        bind(ItemService.class)
                .toProvider(ItemServiceProvider.class)
                .in(scope);

        bind(SimpleInventoryItemService.class)
                .toProvider(SimpleInventoryItemServiceProvider.class)
                .in(scope);

        bind(AdvancedInventoryItemService.class)
                .toProvider(AdvancedInventoryItemServiceProvider.class)
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

        bind(FirebaseAuthService.class)
                .toProvider(FirebaseAuthServiceProvider.class)
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

        bind(AppleSignInAuthService.class)
                .toProvider(AppleSignInAuthServiceProvider.class)
                .in(scope);

        bind(NeoWalletService.class)
                .toProvider(NeoWalletServiceProvider.class)
                .in(scope);

        bind(BscWalletService.class)
                .toProvider(BscWalletServiceProvider.class)
                .in(scope);

        bind(AuthSchemeService.class)
                .toProvider(AuthSchemeServiceProvider.class)
                .in(scope);

        bind(NeoTokenService.class)
                .toProvider(NeoTokenServiceProvider.class)
                .in(scope);

        bind(BscTokenService.class)
                .toProvider(BscTokenServiceProvider.class)
                .in(scope);

        bind(MetadataSpecService.class)
                .toProvider(MetadataSpecServiceProvider.class)
                .in(scope);

        bind(TokenTemplateService.class)
                .toProvider(TokenTemplateServiceProvider.class)
                .in(scope);

        bind(NeoSmartContractService.class)
                .toProvider(NeoSmartContractServiceProvider.class)
                .in(scope);

        bind(BscSmartContractService.class)
                .toProvider(BscSmartContractServiceProvider.class)
                .in(scope);

        bind(HealthStatusService.class)
                .to(DefaultHealthStatusService.class)
                .in(scope);

        bind(SaveDataDocumentService.class)
                .toProvider(SaveDataDocumentServiceProvider.class)
                .in(scope);

        bind(DistinctInventoryItemService.class)
                .toProvider(DistinctInventoryItemServiceProvider.class)
                .in(scope);

        bind(FormidiumService.class)
                .toProvider(FormidiumServiceProvider.class)
                .in(scope);

        bind(WalletService.class)
                .toProvider(WalletServiceProvider.class)
                .in(scope);

        bind(SmartContractService.class)
                .toProvider(SmartContractServiceProvider.class)
                .in(scope);

        bind(VaultService.class)
                .toProvider(VaultServiceProvider.class)
                .in(scope);

        bind(EvmSmartContractService.class)
                .toProvider(EvmSmartContractServiceProvider.class)
                .in(scope);

    }

}
