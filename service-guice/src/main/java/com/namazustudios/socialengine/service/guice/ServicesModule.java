package com.namazustudios.socialengine.service.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scope;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.security.SecureRandomPasswordGenerator;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.advancement.AdvancementService;
import com.namazustudios.socialengine.service.advancement.StandardAdvancementService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.application.*;
import com.namazustudios.socialengine.service.auth.*;
import com.namazustudios.socialengine.service.blockchain.bsc.*;
import com.namazustudios.socialengine.service.blockchain.crypto.*;
import com.namazustudios.socialengine.service.blockchain.neo.*;
import com.namazustudios.socialengine.service.blockchain.omni.*;
import com.namazustudios.socialengine.service.follower.FollowerServiceProvider;
import com.namazustudios.socialengine.service.follower.SuperUserFollowerService;
import com.namazustudios.socialengine.service.formidium.FormidiumService;
import com.namazustudios.socialengine.service.formidium.FormidiumServiceProvider;
import com.namazustudios.socialengine.service.formidium.SuperuserFormidiumService;
import com.namazustudios.socialengine.service.friend.FacebookFriendServiceProvider;
import com.namazustudios.socialengine.service.friend.FriendServiceProvider;
import com.namazustudios.socialengine.service.goods.ItemServiceProvider;
import com.namazustudios.socialengine.service.goods.SuperuserItemService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.health.DefaultHealthStatusService;
import com.namazustudios.socialengine.service.inventory.*;
import com.namazustudios.socialengine.service.leaderboard.LeaderboardServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.RankServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.ScoreServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.SuperUserLeaderboardService;
import com.namazustudios.socialengine.service.manifest.ManifestServiceProvider;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.match.StandardMatchServiceUtils;
import com.namazustudios.socialengine.service.mission.MissionService;
import com.namazustudios.socialengine.service.mission.MissionServiceProvider;
import com.namazustudios.socialengine.service.mission.SuperUserMissionService;
import com.namazustudios.socialengine.service.name.SimpleAdjectiveAnimalNameService;
import com.namazustudios.socialengine.service.notification.FCMRegistrationServiceProvider;
import com.namazustudios.socialengine.service.notification.SuperUserFCMRegistrationService;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileServiceProvider;
import com.namazustudios.socialengine.service.profile.SuperUserProfileOverrideService;
import com.namazustudios.socialengine.service.profile.SuperUserProfileService;
import com.namazustudios.socialengine.service.progress.ProgressService;
import com.namazustudios.socialengine.service.progress.ProgressServiceProvider;
import com.namazustudios.socialengine.service.progress.SuperUserProgressService;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceService;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceServiceProvider;
import com.namazustudios.socialengine.service.savedata.SaveDataDocumentServiceProvider;
import com.namazustudios.socialengine.service.savedata.SuperUserSaveDataDocumentService;
import com.namazustudios.socialengine.service.schema.*;
import com.namazustudios.socialengine.service.shortlink.ShortLinkServiceProvider;
import com.namazustudios.socialengine.service.shortlink.SuperuserShortLinkService;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.social.SuperuserSocialCampaignService;
import com.namazustudios.socialengine.service.user.SuperuserUserService;
import com.namazustudios.socialengine.service.user.UserServiceProvider;
import com.namazustudios.socialengine.service.util.CipherUtility;
import com.namazustudios.socialengine.service.util.CryptoKeyPairUtility;
import com.namazustudios.socialengine.service.util.StandardCipherUtility;
import com.namazustudios.socialengine.service.util.StandardCryptoKeyPairUtility;
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
        
        install(new DatabaseHealthStatusDaoAggregator());

        bind(CipherUtility.class)
            .to(StandardCipherUtility.class)
            .asEagerSingleton();

        bind(CryptoKeyPairUtility.class)
            .to(StandardCryptoKeyPairUtility.class)
            .asEagerSingleton();

        bind(VaultCryptoUtilities.class)
            .to(AesVaultCryptoUtilities.class)
            .asEagerSingleton();

        bind(WalletCryptoUtilities.class)
            .to(StandardWalletCryptoUtilities.class)
            .asEagerSingleton();

        bind(Mapper.class)
            .toProvider(ServicesDozerMapperProvider.class)
            .asEagerSingleton();

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

        bind(NameService.class)
            .to(SimpleAdjectiveAnimalNameService.class)
            .asEagerSingleton();

        bind(CustomAuthSessionService.class)
            .to(StandardCustomAuthSessionService.class);

        bind(AdvancementService.class).to(StandardAdvancementService.class);

        bind(SessionService.class).to(DefaultSessionService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();

        bind(Attributes.class).toProvider(attributesProvider);
        bind(MatchServiceUtils.class).to(StandardMatchServiceUtils.class);

        bind(PasswordGenerator.class).to(SecureRandomPasswordGenerator.class).asEagerSingleton();

        bind(Neow3jClient.class).to(StandardNeow3jClient.class).asEagerSingleton();

        bind(Bscw3jClient.class).to(StandardBscw3jClient.class).asEagerSingleton();

        bind(UsernamePasswordAuthService.class)
            .annotatedWith(Unscoped.class)
            .to(AnonUsernamePasswordAuthService.class);

        bind(SocialCampaignService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperuserSocialCampaignService.class);

        bind(UserService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperuserUserService.class);

        bind(ShortLinkService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperuserShortLinkService.class);

        bind(ApplicationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserApplicationService.class);

        bind(ApplicationConfigurationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserApplicationConfigurationService.class);

        bind(FacebookApplicationConfigurationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserFacebookApplicationConfigurationService.class);

        bind(MatchmakingApplicationConfigurationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserMatchmakingApplicationConfigurationService.class);

        bind(FirebaseApplicationConfigurationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserFirebaseApplicationConfigurationService.class);

        bind(IosApplicationConfigurationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserIosApplicationConfigurationService.class);

        bind(GooglePlayApplicationConfigurationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserGooglePlayApplicationConfigurationService.class);

        bind(ProfileService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserProfileService.class);

        bind(FollowerService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserFollowerService.class);

        bind(ProfileOverrideService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserProfileOverrideService.class);

        bind(FCMRegistrationService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserFCMRegistrationService.class);

        bind(ScoreService.class)
            .annotatedWith(Unscoped.class)
            .to(ScoreService.class);

        bind(LeaderboardService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserLeaderboardService.class);

        bind(MockSessionService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserMockSessionService.class);

        bind(ItemService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperuserItemService.class);

        bind(SimpleInventoryItemService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserSimpleInventoryItemService.class);

        bind(AdvancedInventoryItemService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserAdvancedInventoryItemService.class);

        bind(MissionService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserMissionService.class);

        bind(ProgressService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserProgressService.class);

        bind(FacebookAuthService.class)
            .annotatedWith(Unscoped.class)
            .to(AnonFacebookAuthService.class);

        bind(FirebaseAuthService.class)
            .annotatedWith(Unscoped.class)
            .to(AnonFirebaseAuthService.class);

        bind(VersionService.class)
            .annotatedWith(Unscoped.class)
            .to(BuildPropertiesVersionService.class);

        bind(SessionService.class)
            .annotatedWith(Unscoped.class)
            .to(DefaultSessionService.class);

        bind(AdvancementService.class)
            .annotatedWith(Unscoped.class)
            .to(StandardAdvancementService.class);

        bind(AppleSignInAuthService.class)
            .annotatedWith(Unscoped.class)
            .to(AnonAppleSignInAuthService.class);

        bind(NameService.class)
            .annotatedWith(Unscoped.class)
            .to(SimpleAdjectiveAnimalNameService.class)
            .asEagerSingleton();

        bind(HealthStatusService.class)
            .annotatedWith(Unscoped.class)
            .to(DefaultHealthStatusService.class)
            .asEagerSingleton();

        bind(NeoWalletService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserNeoWalletService.class);

        bind(BscWalletService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserBscWalletService.class);

        bind(Neow3jClient.class)
            .annotatedWith(Unscoped.class)
            .to(StandardNeow3jClient.class)
            .asEagerSingleton();

        bind(Bscw3jClient.class)
            .annotatedWith(Unscoped.class)
            .to(StandardBscw3jClient.class)
            .asEagerSingleton();

        bind(NeoTokenService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserNeoTokenService.class);

        bind(BscTokenService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserBscTokenService.class);

        bind(MetadataSpecService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserMetadataSpecService.class);

        bind(TokenTemplateService.class)
            .annotatedWith(Unscoped.class)
            .to(UserTokenTemplateService.class);

        bind(AuthSchemeService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserAuthSchemeService.class);

        bind(NeoSmartContractService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserNeoSmartContractService.class);

        bind(BscSmartContractService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserBscSmartContractService.class);

        bind(SaveDataDocumentService.class)
            .annotatedWith(Unscoped.class)
            .to(SuperUserSaveDataDocumentService.class);

        bind(CustomAuthSessionService.class)
            .annotatedWith(Unscoped.class)
            .to(StandardCustomAuthSessionService.class);

        bind(DistinctInventoryItemService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserDistinctInventoryItemService.class);

        bind(FormidiumService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperuserFormidiumService.class);

        bind(WalletService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserWalletService.class);

        bind(VaultService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserVaultService.class);

        bind(SmartContractService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserSmartContractService.class);

        bind(WalletIdentityFactory.class)
                .to(StandardWalletIdentityFactory.class)
                .asEagerSingleton();

        // Exposes Scoped Services
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
        expose(FollowerService.class);
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
        expose(ItemService.class);
        expose(SimpleInventoryItemService.class);
        expose(AdvancedInventoryItemService.class);
        expose(MissionService.class);
        expose(ProgressService.class);
        expose(FacebookAuthService.class);
        expose(FirebaseAuthService.class);
        expose(VersionService.class);
        expose(SessionService.class);
        expose(RewardIssuanceService.class);
        expose(AppleIapReceiptService.class);
        expose(GooglePlayIapReceiptService.class);
        expose(AdvancementService.class);
        expose(AppleSignInAuthService.class);
        expose(NameService.class);
        expose(HealthStatusService.class);
        expose(NeoWalletService.class);
        expose(Neow3jClient.class);
        expose(NeoTokenService.class);
        expose(MetadataSpecService.class);
        expose(TokenTemplateService.class);
        expose(NeoSmartContractService.class);
        expose(BscSmartContractService.class);
        expose(AuthSchemeService.class);
        expose(SaveDataDocumentService.class);
        expose(NeoSmartContractService.class);
        expose(BscSmartContractService.class);
        expose(CustomAuthSessionService.class);
        expose(DistinctInventoryItemService.class);
        expose(BscWalletService.class);
        expose(Bscw3jClient.class);
        expose(BscTokenService.class);
        expose(FormidiumService.class);
        expose(WalletService.class);
        expose(VaultService.class);
        expose(SmartContractService.class);

        // Unscoped Services
        expose(UsernamePasswordAuthService.class).annotatedWith(Unscoped.class);
        expose(SocialCampaignService.class).annotatedWith(Unscoped.class);
        expose(UserService.class).annotatedWith(Unscoped.class);
        expose(ShortLinkService.class).annotatedWith(Unscoped.class);
        expose(ApplicationService.class).annotatedWith(Unscoped.class);
        expose(ApplicationConfigurationService.class).annotatedWith(Unscoped.class);
        expose(FacebookApplicationConfigurationService.class).annotatedWith(Unscoped.class);
        expose(MatchmakingApplicationConfigurationService.class).annotatedWith(Unscoped.class);
        expose(FirebaseApplicationConfigurationService.class).annotatedWith(Unscoped.class);
        expose(IosApplicationConfigurationService.class).annotatedWith(Unscoped.class);
        expose(GooglePlayApplicationConfigurationService.class).annotatedWith(Unscoped.class);
        expose(ProfileService.class).annotatedWith(Unscoped.class);
        expose(FollowerService.class).annotatedWith(Unscoped.class);
        expose(ProfileOverrideService.class).annotatedWith(Unscoped.class);
        expose(FCMRegistrationService.class).annotatedWith(Unscoped.class);
        expose(ScoreService.class).annotatedWith(Unscoped.class);
        expose(LeaderboardService.class).annotatedWith(Unscoped.class);
        expose(MockSessionService.class).annotatedWith(Unscoped.class);

        expose(ItemService.class).annotatedWith(Unscoped.class);
        expose(SimpleInventoryItemService.class).annotatedWith(Unscoped.class);
        expose(AdvancedInventoryItemService.class).annotatedWith(Unscoped.class);
        expose(MissionService.class).annotatedWith(Unscoped.class);
        expose(ProgressService.class).annotatedWith(Unscoped.class);
        expose(FacebookAuthService.class).annotatedWith(Unscoped.class);
        expose(FirebaseAuthService.class).annotatedWith(Unscoped.class);
        expose(VersionService.class).annotatedWith(Unscoped.class);
        expose(SessionService.class).annotatedWith(Unscoped.class);
        expose(AdvancementService.class).annotatedWith(Unscoped.class);
        expose(AppleSignInAuthService.class).annotatedWith(Unscoped.class);
        expose(NameService.class).annotatedWith(Unscoped.class);
        expose(HealthStatusService.class).annotatedWith(Unscoped.class);
        expose(NeoWalletService.class).annotatedWith(Unscoped.class);
        expose(Neow3jClient.class).annotatedWith(Unscoped.class);
        expose(NeoTokenService.class).annotatedWith(Unscoped.class);
        expose(MetadataSpecService.class).annotatedWith(Unscoped.class);
        expose(TokenTemplateService.class).annotatedWith(Unscoped.class);
        expose(NeoSmartContractService.class).annotatedWith(Unscoped.class);
        expose(BscSmartContractService.class).annotatedWith(Unscoped.class);
        expose(AuthSchemeService.class).annotatedWith(Unscoped.class);
        expose(SaveDataDocumentService.class).annotatedWith(Unscoped.class);
        expose(NeoSmartContractService.class).annotatedWith(Unscoped.class);
        expose(CustomAuthSessionService.class).annotatedWith(Unscoped.class);
        expose(DistinctInventoryItemService.class).annotatedWith(Unscoped.class);
        expose(BscWalletService.class).annotatedWith(Unscoped.class);
        expose(Bscw3jClient.class).annotatedWith(Unscoped.class);
        expose(BscTokenService.class).annotatedWith(Unscoped.class);
        expose(FormidiumService.class).annotatedWith(Unscoped.class);
        expose(WalletService.class).annotatedWith(Unscoped.class);
        expose(SmartContractService.class).annotatedWith(Unscoped.class);
        expose(VaultService.class).annotatedWith(Unscoped.class);

    }

}
