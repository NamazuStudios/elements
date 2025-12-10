package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.application.*;
import dev.getelements.elements.sdk.service.auth.*;
import dev.getelements.elements.sdk.service.blockchain.*;
import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.service.firebase.FCMRegistrationService;
import dev.getelements.elements.sdk.service.follower.FollowerService;
import dev.getelements.elements.sdk.service.friend.FriendService;
import dev.getelements.elements.sdk.service.goods.ItemService;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.sdk.service.health.HealthStatusService;
import dev.getelements.elements.sdk.service.index.IndexService;
import dev.getelements.elements.sdk.service.inventory.AdvancedInventoryItemService;
import dev.getelements.elements.sdk.service.inventory.DistinctInventoryItemService;
import dev.getelements.elements.sdk.service.inventory.SimpleInventoryItemService;
import dev.getelements.elements.sdk.service.invite.InviteService;
import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import dev.getelements.elements.sdk.service.leaderboard.LeaderboardService;
import dev.getelements.elements.sdk.service.leaderboard.RankService;
import dev.getelements.elements.sdk.service.leaderboard.ScoreService;
import dev.getelements.elements.sdk.service.match.MatchService;
import dev.getelements.elements.sdk.service.match.MultiMatchService;
import dev.getelements.elements.sdk.service.metadata.MetadataService;
import dev.getelements.elements.sdk.service.mission.MissionService;
import dev.getelements.elements.sdk.service.mission.ScheduleEventService;
import dev.getelements.elements.sdk.service.mission.ScheduleProgressService;
import dev.getelements.elements.sdk.service.mission.ScheduleService;
import dev.getelements.elements.sdk.service.profile.ProfileOverrideService;
import dev.getelements.elements.sdk.service.profile.ProfileService;
import dev.getelements.elements.sdk.service.progress.ProgressService;
import dev.getelements.elements.sdk.service.rewardissuance.RewardIssuanceService;
import dev.getelements.elements.sdk.service.savedata.SaveDataDocumentService;
import dev.getelements.elements.sdk.service.schema.MetadataSpecService;
import dev.getelements.elements.sdk.service.user.UserService;
import dev.getelements.elements.service.appleiap.AppleIapReceiptServiceProvider;
import dev.getelements.elements.service.appleiap.UserAppleIapReceiptService;
import dev.getelements.elements.service.application.*;
import dev.getelements.elements.service.auth.*;
import dev.getelements.elements.service.blockchain.crypto.evm.EvmSmartContractServiceProvider;
import dev.getelements.elements.service.blockchain.crypto.evm.SuperUserEvmSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.crypto.flow.FlowSmartContractInvocationServiceProvider;
import dev.getelements.elements.service.blockchain.crypto.flow.SuperUserFlowSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.crypto.near.NearSmartContractServiceProvider;
import dev.getelements.elements.service.blockchain.crypto.near.SuperUserNearSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.omni.*;
import dev.getelements.elements.service.cdn.AnonCdnDeploymentService;
import dev.getelements.elements.service.cdn.CdnDeploymentServiceProvider;
import dev.getelements.elements.service.cdn.SuperuserDeploymentService;
import dev.getelements.elements.service.codegen.OpenApiCodegenServiceProvider;
import dev.getelements.elements.service.follower.FollowerServiceProvider;
import dev.getelements.elements.service.follower.SuperUserFollowerService;
import dev.getelements.elements.service.follower.UserFollowerService;
import dev.getelements.elements.service.friend.FriendServiceProvider;
import dev.getelements.elements.service.friend.UserFriendService;
import dev.getelements.elements.service.goods.AnonItemService;
import dev.getelements.elements.service.goods.ItemServiceProvider;
import dev.getelements.elements.service.goods.SuperuserItemService;
import dev.getelements.elements.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import dev.getelements.elements.service.health.DefaultHealthStatusService;
import dev.getelements.elements.service.index.IndexServiceProvider;
import dev.getelements.elements.service.index.SuperUserIndexService;
import dev.getelements.elements.service.inventory.*;
import dev.getelements.elements.service.invite.InviteServiceProvider;
import dev.getelements.elements.service.invite.SuperUserInviteService;
import dev.getelements.elements.service.invite.UserInviteService;
import dev.getelements.elements.service.largeobject.AnonLargeObjectService;
import dev.getelements.elements.service.largeobject.LargeObjectServiceProvider;
import dev.getelements.elements.service.largeobject.SuperUserLargeObjectService;
import dev.getelements.elements.service.largeobject.UserLargeObjectService;
import dev.getelements.elements.service.leaderboard.*;
import dev.getelements.elements.service.match.*;
import dev.getelements.elements.service.metadata.AnonMetadataService;
import dev.getelements.elements.service.metadata.MetadataServiceProvider;
import dev.getelements.elements.service.metadata.SuperUserMetadataService;
import dev.getelements.elements.service.metadata.UserMetadataService;
import dev.getelements.elements.service.mission.*;
import dev.getelements.elements.service.notification.FCMRegistrationServiceProvider;
import dev.getelements.elements.service.notification.SuperUserFCMRegistrationService;
import dev.getelements.elements.service.notification.UserFCMRegistrationService;
import dev.getelements.elements.service.profile.*;
import dev.getelements.elements.service.progress.ProgressServiceProvider;
import dev.getelements.elements.service.progress.SuperUserProgressService;
import dev.getelements.elements.service.progress.UserProgressService;
import dev.getelements.elements.service.rewardissuance.RewardIssuanceServiceProvider;
import dev.getelements.elements.service.rewardissuance.UserRewardIssuanceService;
import dev.getelements.elements.service.savedata.SaveDataDocumentServiceProvider;
import dev.getelements.elements.service.savedata.SuperUserSaveDataDocumentService;
import dev.getelements.elements.service.savedata.UserSaveDataDocumentService;
import dev.getelements.elements.service.schema.MetadataSpecServiceProvider;
import dev.getelements.elements.service.schema.SuperUserMetadataSpecService;
import dev.getelements.elements.service.user.AnonUserService;
import dev.getelements.elements.service.user.SuperuserUserService;
import dev.getelements.elements.service.user.UserServiceProvider;
import dev.getelements.elements.service.user.UserUserService;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.service.Constants.*;

public class ScopedServicesModule extends AbstractModule {

    private final Scope scope;

    public ScopedServicesModule(final Scope scope) {
        this.scope = scope;
    }

    @Override
    protected void configure() {
        bindProviders();
        bindAnonymous();
        bindUser();
        bindSuperUser();
    }

    private void bindProviders() {

        bind(UsernamePasswordAuthService.class)
                .toProvider(UsernamePasswordAuthServiceProvider.class)
                .in(scope);

        bind(OidcAuthService.class)
                .toProvider(OidcAuthServiceProvider.class)
                .in(scope);

        bind(OAuth2AuthService.class)
                .toProvider(OAuth2AuthServiceProvider.class)
                .in(scope);

        bind(UserService.class)
                .toProvider(UserServiceProvider.class)
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

        bind(FollowerService.class)
                .toProvider(FollowerServiceProvider.class)
                .in(scope);

        bind(MatchService.class)
                .toProvider(MatchServiceProvider.class)
                .in(scope);

        bind(MultiMatchService.class)
                .toProvider(MultiMatchServiceProvider.class)
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

        bind(AuthSchemeService.class)
                .toProvider(AuthSchemeServiceProvider.class)
                .in(scope);

        bind(OidcAuthSchemeService.class)
                .toProvider(OidcAuthSchemeServiceProvider.class)
                .in(scope);

        bind(OAuth2AuthSchemeService.class)
                .toProvider(OAuth2AuthSchemeServiceProvider.class)
                .in(scope);

        bind(MetadataService.class)
                .toProvider(MetadataServiceProvider.class)
                .in(scope);

        bind(MetadataSpecService.class)
                .toProvider(MetadataSpecServiceProvider.class)
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

        bind(WalletService.class)
                .toProvider(WalletServiceProvider.class)
                .in(scope);

        bind(SmartContractService.class)
                .toProvider(SmartContractServiceProvider.class)
                .in(scope);

        bind(VaultService.class)
                .toProvider(VaultServiceProvider.class)
                .in(scope);

        bind(EvmSmartContractInvocationService.class)
                .toProvider(EvmSmartContractServiceProvider.class)
                .in(scope);

        bind(FlowSmartContractInvocationService.class)
                .toProvider(FlowSmartContractInvocationServiceProvider.class)
                .in(scope);

        bind(NearSmartContractInvocationService.class)
                .toProvider(NearSmartContractServiceProvider.class)
                .in(scope);

        bind(LargeObjectService.class)
                .toProvider(LargeObjectServiceProvider.class)
                .in(scope);

        bind(IndexService.class)
                .toProvider(IndexServiceProvider.class)
                .in(scope);

        bind(InviteService.class)
                .toProvider(InviteServiceProvider.class)
                .in(scope);

        bind(ScheduleService.class)
                .toProvider(ScheduleServiceProvider.class)
                .in(scope);

        bind(ScheduleEventService.class)
                .toProvider(ScheduleEventServiceProvider.class)
                .in(scope);

        bind(ScheduleProgressService.class)
                .toProvider(ScheduleProgressServiceProvider.class)
                .in(scope);

        bind(CdnDeploymentService.class)
                .toProvider(CdnDeploymentServiceProvider.class)
                .in(scope);

        bind(CodegenService.class)
                .toProvider(OpenApiCodegenServiceProvider.class)
                .in(scope);

        bind(ApplicationStatusService.class)
                .toProvider(ApplicationStatusServiceProvider.class)
                .in(scope);

    }

    private void bindAnonymous() {
        
        bind(ApplicationService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonApplicationService.class);

        bind(ApplicationConfigurationService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonApplicationConfigurationService.class);

        bind(CdnDeploymentService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonCdnDeploymentService.class);

        bind(FacebookApplicationConfigurationService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonFacebookApplicationConfigurationService.class);

        bind(GooglePlayApplicationConfigurationService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonGooglePlayApplicationConfigurationService.class);

        bind(IosApplicationConfigurationService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonIosApplicationConfigurationService.class);

        bind(ItemService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonItemService.class);

        bind(LargeObjectService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonLargeObjectService.class);

        bind(LeaderboardService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonLeaderboardService.class);

        bind(MetadataService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonMetadataService.class);

        bind(MissionService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonMissionService.class);

        bind(OidcAuthService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonOidcAuthService.class);

        bind(OAuth2AuthService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonOAuth2AuthService.class);

        bind(ProfileOverrideService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonProfileOverrideService.class);

        bind(RankService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonRankService.class);

        bind(UserService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonUserService.class);

        bind(UsernamePasswordAuthService.class)
                .annotatedWith(named(ANONYMOUS))
                .to(AnonUsernamePasswordAuthService.class);

    }

    private void bindUser() {

        bind(AdvancedInventoryItemService.class)
                .annotatedWith(named(USER))
                .to(UserAdvancedInventoryItemService.class);

        bind(AppleIapReceiptService.class)
                .annotatedWith(named(USER))
                .to(UserAppleIapReceiptService.class);

        bind(DistinctInventoryItemService.class)
                .annotatedWith(named(USER))
                .to(UserDistinctInventoryItemService.class);

        bind(FCMRegistrationService.class)
                .annotatedWith(named(USER))
                .to(UserFCMRegistrationService.class);

        bind(FollowerService.class)
                .annotatedWith(named(USER))
                .to(UserFollowerService.class);

        bind(FriendService.class)
                .annotatedWith(named(USER))
                .to(UserFriendService.class);

        bind(InviteService.class)
                .annotatedWith(named(USER))
                .to(UserInviteService.class);

        bind(LargeObjectService.class)
                .annotatedWith(named(USER))
                .to(UserLargeObjectService.class);

        bind(MatchService.class)
                .annotatedWith(named(USER))
                .to(UserMatchService.class);

        bind(MultiMatchService.class)
                .annotatedWith(named(USER))
                .to(UserMultiMatchService.class);

        bind(MetadataService.class)
                .annotatedWith(named(USER))
                .to(UserMetadataService.class);

        bind(OidcAuthService.class)
                .annotatedWith(named(USER))
                .to(UserOidcAuthService.class);

        bind(OAuth2AuthService.class)
                .annotatedWith(named(USER))
                .to(UserOAuth2AuthService.class);

        bind(ProfileOverrideService.class)
                .annotatedWith(named(USER))
                .to(UserProfileOverrideService.class);

        bind(ProfileService.class)
                .annotatedWith(named(USER))
                .to(UserProfileService.class);

        bind(ProgressService.class)
                .annotatedWith(named(USER))
                .to(UserProgressService.class);

        bind(RankService.class)
                .annotatedWith(named(USER))
                .to(UserRankService.class);

        bind(RewardIssuanceService.class)
                .annotatedWith(named(USER))
                .to(UserRewardIssuanceService.class);

        bind(SaveDataDocumentService.class)
                .annotatedWith(named(USER))
                .to(UserSaveDataDocumentService.class);

        bind(ScoreService.class)
                .annotatedWith(named(USER))
                .to(UserScoreService.class);

        bind(SimpleInventoryItemService.class)
                .annotatedWith(named(USER))
                .to(UserSimpleInventoryItemService.class);

        bind(SaveDataDocumentService.class)
                .annotatedWith(named(USER))
                .to(UserSaveDataDocumentService.class);

        bind(ScheduleProgressService.class)
                .annotatedWith(named(USER))
                .to(UserScheduleProgressService.class);

        bind(UserService.class)
                .annotatedWith(named(USER))
                .to(UserUserService.class);

        bind(UsernamePasswordAuthService.class)
                .annotatedWith(named(USER))
                .to(UserUsernamePasswordAuthService.class);

        bind(VaultService.class)
                .annotatedWith(named(USER))
                .to(UserVaultService.class);

        bind(WalletService.class)
                .annotatedWith(named(USER))
                .to(UserWalletService.class);

    }

    private void bindSuperUser() {

        bind(AdvancedInventoryItemService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserAdvancedInventoryItemService.class);

        bind(ApplicationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserApplicationService.class);

        bind(ApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserApplicationConfigurationService.class);

        bind(AuthSchemeService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserAuthSchemeService.class);

        bind(CdnDeploymentService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperuserDeploymentService.class);

        bind(DistinctInventoryItemService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserDistinctInventoryItemService.class);

        bind(FacebookApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserFacebookApplicationConfigurationService.class);

        bind(FlowSmartContractInvocationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserFlowSmartContractInvocationService.class);

        bind(EvmSmartContractInvocationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserEvmSmartContractInvocationService.class);

        bind(FCMRegistrationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserFCMRegistrationService.class);

        bind(FirebaseApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserFirebaseApplicationConfigurationService.class);

        bind(FollowerService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserFollowerService.class);

        bind(GooglePlayApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserGooglePlayApplicationConfigurationService.class);

        bind(IndexService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserIndexService.class);

        bind(InviteService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserInviteService.class);

        bind(IosApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserIosApplicationConfigurationService.class);

        bind(ItemService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperuserItemService.class);

        bind(LargeObjectService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserLargeObjectService.class);

        bind(LeaderboardService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserLeaderboardService.class);

        bind(MatchmakingApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserMatchmakingApplicationConfigurationService.class);

        bind(MetadataService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserMetadataService.class);

        bind(MetadataSpecService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserMetadataSpecService.class);

        bind(MissionService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserMissionService.class);

        bind(MockSessionService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserMockSessionService.class);

        bind(MultiMatchService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperuserMultiMatchService.class);

        bind(NearSmartContractInvocationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserNearSmartContractInvocationService.class);

        bind(OAuth2AuthSchemeService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserOAuth2AuthSchemeService.class);

        bind(OidcAuthSchemeService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserOidcAuthSchemeService.class);

        bind(ProfileOverrideService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserProfileOverrideService.class);

        bind(ProfileService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserProfileService.class);

        bind(ProgressService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserProgressService.class);

        bind(PSNApplicationConfigurationService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserPSNApplicationConfigurationService.class);

        bind(RankService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserRankService.class);

        bind(SaveDataDocumentService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserSaveDataDocumentService.class);

        bind(SimpleInventoryItemService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserSimpleInventoryItemService.class);

        bind(SaveDataDocumentService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserSaveDataDocumentService.class);

        bind(ScheduleService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserScheduleService.class);

        bind(ScheduleEventService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserScheduleEventService.class);

        bind(SmartContractService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserSmartContractService.class);

        bind(UserService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperuserUserService.class);

        bind(VaultService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserVaultService.class);

        bind(WalletService.class)
                .annotatedWith(named(SUPERUSER))
                .to(SuperUserWalletService.class);
    }

}
