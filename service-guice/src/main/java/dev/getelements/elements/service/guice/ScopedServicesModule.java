package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;

import dev.getelements.elements.sdk.service.auth.*;
import dev.getelements.elements.sdk.service.application.*;
import dev.getelements.elements.sdk.service.blockchain.*;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.service.inventory.*;
import dev.getelements.elements.sdk.service.invite.InviteService;
import dev.getelements.elements.sdk.service.user.*;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import dev.getelements.elements.sdk.service.firebase.FCMRegistrationService;
import dev.getelements.elements.sdk.service.follower.FollowerService;
import dev.getelements.elements.sdk.service.friend.FriendService;
import dev.getelements.elements.sdk.service.goods.ItemService;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.sdk.service.health.HealthStatusService;
import dev.getelements.elements.sdk.service.index.IndexService;
import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import dev.getelements.elements.sdk.service.leaderboard.LeaderboardService;
import dev.getelements.elements.sdk.service.leaderboard.RankService;
import dev.getelements.elements.sdk.service.leaderboard.ScoreService;
import dev.getelements.elements.sdk.service.match.MatchService;
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
import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;

import dev.getelements.elements.service.appleiap.AppleIapReceiptServiceProvider;
import dev.getelements.elements.service.application.*;
import dev.getelements.elements.service.auth.*;
import dev.getelements.elements.service.blockchain.crypto.evm.EvmSmartContractServiceProvider;
import dev.getelements.elements.service.blockchain.crypto.flow.FlowSmartContractInvocationServiceProvider;
import dev.getelements.elements.service.blockchain.crypto.near.NearSmartContractServiceProvider;
import dev.getelements.elements.service.blockchain.omni.SmartContractServiceProvider;
import dev.getelements.elements.service.blockchain.omni.VaultServiceProvider;
import dev.getelements.elements.service.blockchain.omni.WalletServiceProvider;
import dev.getelements.elements.service.cdn.CdnDeploymentServiceProvider;
import dev.getelements.elements.service.codegen.OpenApiCodegenServiceProvider;
import dev.getelements.elements.service.follower.FollowerServiceProvider;
import dev.getelements.elements.service.friend.FriendServiceProvider;
import dev.getelements.elements.service.goods.ItemServiceProvider;
import dev.getelements.elements.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import dev.getelements.elements.service.health.DefaultHealthStatusService;
import dev.getelements.elements.service.index.IndexServiceProvider;
import dev.getelements.elements.service.inventory.AdvancedInventoryItemServiceProvider;
import dev.getelements.elements.service.inventory.DistinctInventoryItemServiceProvider;
import dev.getelements.elements.service.inventory.SimpleInventoryItemServiceProvider;
import dev.getelements.elements.service.invite.InviteServiceProvider;
import dev.getelements.elements.service.largeobject.LargeObjectServiceProvider;
import dev.getelements.elements.service.leaderboard.LeaderboardServiceProvider;
import dev.getelements.elements.service.leaderboard.RankServiceProvider;
import dev.getelements.elements.service.leaderboard.ScoreServiceProvider;
import dev.getelements.elements.service.match.MatchServiceProvider;
import dev.getelements.elements.service.mission.MissionServiceProvider;
import dev.getelements.elements.service.mission.ScheduleEventServiceProvider;
import dev.getelements.elements.service.mission.ScheduleProgressServiceProvider;
import dev.getelements.elements.service.mission.ScheduleServiceProvider;
import dev.getelements.elements.service.notification.FCMRegistrationServiceProvider;
import dev.getelements.elements.service.profile.ProfileOverrideServiceProvider;
import dev.getelements.elements.service.profile.ProfileServiceProvider;
import dev.getelements.elements.service.progress.ProgressServiceProvider;
import dev.getelements.elements.service.rewardissuance.RewardIssuanceServiceProvider;
import dev.getelements.elements.service.savedata.SaveDataDocumentServiceProvider;
import dev.getelements.elements.service.schema.MetadataSpecServiceProvider;
import dev.getelements.elements.service.user.UserServiceProvider;

public class ScopedServicesModule extends AbstractModule {

    private final Scope scope;

    public ScopedServicesModule(final Scope scope) {
        this.scope = scope;
    }

    @Override
    protected void configure() {

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
    }

}
