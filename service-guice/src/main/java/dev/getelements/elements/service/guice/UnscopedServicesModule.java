package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.*;
import dev.getelements.elements.service.advancement.AdvancementService;
import dev.getelements.elements.service.advancement.StandardAdvancementService;
import dev.getelements.elements.service.application.*;
import dev.getelements.elements.service.auth.*;
import dev.getelements.elements.service.blockchain.invoke.evm.SuperUserEvmSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.flow.SuperUserFlowSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.near.SuperUserNearSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.omni.SuperUserSmartContractService;
import dev.getelements.elements.service.blockchain.omni.SuperUserVaultService;
import dev.getelements.elements.service.blockchain.omni.SuperUserWalletService;
import dev.getelements.elements.service.follower.SuperUserFollowerService;
import dev.getelements.elements.service.formidium.FormidiumService;
import dev.getelements.elements.service.formidium.SuperuserFormidiumService;
import dev.getelements.elements.service.goods.SuperuserItemService;
import dev.getelements.elements.service.health.DefaultHealthStatusService;
import dev.getelements.elements.service.inventory.*;
import dev.getelements.elements.service.largeobject.SuperUserLargeObjectService;
import dev.getelements.elements.service.leaderboard.SuperUserLeaderboardService;
import dev.getelements.elements.service.mission.MissionService;
import dev.getelements.elements.service.mission.SuperUserMissionService;
import dev.getelements.elements.service.name.SimpleAdjectiveAnimalNameService;
import dev.getelements.elements.service.notification.SuperUserFCMRegistrationService;
import dev.getelements.elements.service.profile.SuperUserProfileOverrideService;
import dev.getelements.elements.service.profile.SuperUserProfileService;
import dev.getelements.elements.service.progress.ProgressService;
import dev.getelements.elements.service.progress.SuperUserProgressService;
import dev.getelements.elements.service.savedata.SuperUserSaveDataDocumentService;
import dev.getelements.elements.service.schema.MetadataSpecService;
import dev.getelements.elements.service.schema.SuperUserMetadataSpecService;
import dev.getelements.elements.service.shortlink.SuperuserShortLinkService;
import dev.getelements.elements.service.user.SuperuserUserService;

public class UnscopedServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(UsernamePasswordAuthService.class)
                .annotatedWith(Unscoped.class)
                .to(AnonUsernamePasswordAuthService.class);

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

        bind(GoogleSignInAuthService.class)
                .annotatedWith(Unscoped.class)
                .to(AnonGoogleSignInAuthService.class);

        bind(NameService.class)
                .annotatedWith(Unscoped.class)
                .to(SimpleAdjectiveAnimalNameService.class)
                .asEagerSingleton();

        bind(HealthStatusService.class)
                .annotatedWith(Unscoped.class)
                .to(DefaultHealthStatusService.class)
                .asEagerSingleton();

        bind(MetadataSpecService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserMetadataSpecService.class);

        bind(AuthSchemeService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserAuthSchemeService.class);

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

        bind(EvmSmartContractInvocationService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserEvmSmartContractInvocationService.class);

        bind(FlowSmartContractInvocationService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserFlowSmartContractInvocationService.class);

        bind(NearSmartContractInvocationService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserNearSmartContractInvocationService.class);

        bind(LargeObjectService.class)
                .annotatedWith(Unscoped.class)
                .to(SuperUserLargeObjectService.class);

    }

}
