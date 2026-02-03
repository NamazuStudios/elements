package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.service.advancement.AdvancementService;
import dev.getelements.elements.sdk.service.blockchain.*;
import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.service.firebase.FCMRegistrationService;
import dev.getelements.elements.sdk.service.follower.FollowerService;
import dev.getelements.elements.sdk.service.goods.ItemService;
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
import dev.getelements.elements.sdk.service.match.MultiMatchService;
import dev.getelements.elements.sdk.service.metadata.MetadataService;
import dev.getelements.elements.sdk.service.mission.MissionService;
import dev.getelements.elements.sdk.service.mission.ScheduleEventService;
import dev.getelements.elements.sdk.service.mission.ScheduleService;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import dev.getelements.elements.sdk.service.system.ElementDeploymentService;
import dev.getelements.elements.service.advancement.StandardAdvancementService;
import dev.getelements.elements.sdk.service.application.*;
import dev.getelements.elements.sdk.service.auth.*;
import dev.getelements.elements.service.auth.oauth2.AnonOAuth2AuthService;
import dev.getelements.elements.service.auth.oauth2.SuperUserOAuth2AuthSchemeService;
import dev.getelements.elements.service.auth.oidc.AnonOidcAuthService;
import dev.getelements.elements.service.auth.oidc.SuperUserOidcAuthSchemeService;
import dev.getelements.elements.service.blockchain.crypto.evm.SuperUserEvmSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.crypto.flow.SuperUserFlowSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.crypto.near.SuperUserNearSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.omni.SuperUserSmartContractService;
import dev.getelements.elements.service.blockchain.omni.SuperUserVaultService;
import dev.getelements.elements.service.blockchain.omni.SuperUserWalletService;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.notification.NotificationService;
import dev.getelements.elements.sdk.service.profile.ProfileOverrideService;
import dev.getelements.elements.sdk.service.profile.ProfileService;
import dev.getelements.elements.sdk.service.progress.ProgressService;
import dev.getelements.elements.sdk.service.savedata.SaveDataDocumentService;
import dev.getelements.elements.sdk.service.schema.MetadataSpecService;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.service.application.*;
import dev.getelements.elements.service.auth.*;
import dev.getelements.elements.service.cdn.SuperuserDeploymentService;
import dev.getelements.elements.service.codegen.SuperUserOpenApiCodegenService;
import dev.getelements.elements.service.defaults.DefaultOAuth2SchemeConfiguration;
import dev.getelements.elements.service.defaults.DefaultOidcSchemeConfiguration;
import dev.getelements.elements.service.defaults.DefaultUserConfiguration;
import dev.getelements.elements.service.follower.SuperUserFollowerService;
import dev.getelements.elements.service.goods.SuperuserItemService;
import dev.getelements.elements.service.health.DefaultHealthStatusService;
import dev.getelements.elements.service.index.SuperUserIndexService;
import dev.getelements.elements.service.inventory.*;
import dev.getelements.elements.service.invite.SuperUserInviteService;
import dev.getelements.elements.service.largeobject.SuperUserLargeObjectService;
import dev.getelements.elements.service.leaderboard.*;
import dev.getelements.elements.service.match.SuperuserMultiMatchService;
import dev.getelements.elements.service.metadata.SuperUserMetadataService;
import dev.getelements.elements.service.mission.*;
import dev.getelements.elements.service.name.SimpleAdjectiveAnimalNameService;
import dev.getelements.elements.service.notification.StandardNotificationService;
import dev.getelements.elements.service.notification.SuperUserFCMRegistrationService;
import dev.getelements.elements.service.profile.SuperUserProfileOverrideService;
import dev.getelements.elements.service.profile.SuperUserProfileService;
import dev.getelements.elements.service.progress.SuperUserProgressService;
import dev.getelements.elements.service.receipt.SuperuserReceiptService;
import dev.getelements.elements.service.savedata.SuperUserSaveDataDocumentService;
import dev.getelements.elements.service.schema.SuperUserMetadataSpecService;
import dev.getelements.elements.service.system.SuperUserElementDeploymentService;
import dev.getelements.elements.service.user.SuperuserUserService;
import dev.getelements.elements.sdk.service.user.UserService;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

public class UnscopedServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(UsernamePasswordAuthService.class)
                .annotatedWith(named(UNSCOPED))
                .to(AnonUsernamePasswordAuthService.class);

        bind(OidcAuthService.class)
                .annotatedWith(named(UNSCOPED))
                .to(AnonOidcAuthService.class);

        bind(OAuth2AuthService.class)
                .annotatedWith(named(UNSCOPED))
                .to(AnonOAuth2AuthService.class);

        bind(UserService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperuserUserService.class);

        bind(ApplicationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserApplicationService.class);

        bind(ApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserApplicationConfigurationService.class);

        bind(FacebookApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserFacebookApplicationConfigurationService.class);

        bind(OculusApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserOculusApplicationConfigurationService.class);

        bind(MatchmakingApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserMatchmakingApplicationConfigurationService.class);

        bind(MultiMatchService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperuserMultiMatchService.class);

        bind(FirebaseApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserFirebaseApplicationConfigurationService.class);

        bind(IosApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserIosApplicationConfigurationService.class);

        bind(GooglePlayApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserGooglePlayApplicationConfigurationService.class);

        bind(ProfileService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserProfileService.class);

        bind(FollowerService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserFollowerService.class);

        bind(ProfileOverrideService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserProfileOverrideService.class);

        bind(FCMRegistrationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserFCMRegistrationService.class);

        bind(ScoreService.class)
                .annotatedWith(named(UNSCOPED))
                .to(ScoreService.class);

        bind(LeaderboardService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserLeaderboardService.class);

        bind(MockSessionService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserMockSessionService.class);

        bind(ItemService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperuserItemService.class);

        bind(SimpleInventoryItemService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserSimpleInventoryItemService.class);

        bind(AdvancedInventoryItemService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserAdvancedInventoryItemService.class);

        bind(MissionService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserMissionService.class);

        bind(ProgressService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserProgressService.class);

        bind(VersionService.class)
                .annotatedWith(named(UNSCOPED))
                .to(BuildPropertiesVersionService.class);

        bind(SessionService.class)
                .annotatedWith(named(UNSCOPED))
                .to(DefaultSessionService.class);

        bind(AdvancementService.class)
                .annotatedWith(named(UNSCOPED))
                .to(StandardAdvancementService.class);

        bind(NameService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SimpleAdjectiveAnimalNameService.class)
                .asEagerSingleton();

        bind(HealthStatusService.class)
                .annotatedWith(named(UNSCOPED))
                .to(DefaultHealthStatusService.class)
                .asEagerSingleton();

        bind(MetadataService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserMetadataService.class);

        bind(MetadataSpecService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserMetadataSpecService.class);

        bind(AuthSchemeService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserAuthSchemeService.class);

        bind(OidcAuthSchemeService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserOidcAuthSchemeService.class);

        bind(OAuth2AuthSchemeService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserOAuth2AuthSchemeService.class);

        bind(SaveDataDocumentService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserSaveDataDocumentService.class);

        bind(CustomAuthSessionService.class)
                .annotatedWith(named(UNSCOPED))
                .to(StandardCustomAuthSessionService.class);

        bind(DistinctInventoryItemService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserDistinctInventoryItemService.class);

        bind(WalletService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserWalletService.class);

        bind(VaultService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserVaultService.class);

        bind(SmartContractService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserSmartContractService.class);

        bind(EvmSmartContractInvocationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserEvmSmartContractInvocationService.class);

        bind(FlowSmartContractInvocationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserFlowSmartContractInvocationService.class);

        bind(NearSmartContractInvocationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserNearSmartContractInvocationService.class);

        bind(LargeObjectService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserLargeObjectService.class);

        bind(InviteService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserInviteService.class);

        bind(ScheduleService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserScheduleService.class);

        bind(ScheduleEventService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserScheduleEventService.class);

        bind(PSNApplicationConfigurationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserPSNApplicationConfigurationService.class);

        bind(CdnDeploymentService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperuserDeploymentService.class);

        bind(IndexService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserIndexService.class);

        bind(RankService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserRankService.class);

        bind(NotificationService.class)
                .annotatedWith(named(UNSCOPED))
                .to(StandardNotificationService.class);

        bind(ApplicationStatusService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserApplicationStatusService.class);

        bind(CodegenService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserOpenApiCodegenService.class);

        bind(ReceiptService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperuserReceiptService.class);

        bind(ElementDeploymentService.class)
                .annotatedWith(named(UNSCOPED))
                .to(SuperUserElementDeploymentService.class);

        bind(DefaultOidcSchemeConfiguration.class).asEagerSingleton();
        bind(DefaultUserConfiguration.class).asEagerSingleton();

        bind(DefaultOAuth2SchemeConfiguration.class).asEagerSingleton();

    }

}
