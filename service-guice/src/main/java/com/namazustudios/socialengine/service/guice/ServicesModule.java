package com.namazustudios.socialengine.service.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scope;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.advancement.AdvancementService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.auth.*;
import com.namazustudios.socialengine.service.blockchain.bsc.*;
import com.namazustudios.socialengine.service.blockchain.neo.*;
import com.namazustudios.socialengine.service.formidium.FormidiumService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.service.inventory.*;
import com.namazustudios.socialengine.service.mission.MissionService;
import com.namazustudios.socialengine.service.progress.ProgressService;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceService;
import com.namazustudios.socialengine.service.schema.*;

import javax.inject.Provider;

/**
 * Configures all of the services, using a {@link Scope} for {@link User}, {@link Profile} injections.
 */
public class ServicesModule extends PrivateModule {

    private static final String INTERNAL_SERVICE_NAME = "com.namazustudios.socialengine.service.guice";

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

        bind(Attributes.class).toProvider(attributesProvider);

        install(new ServiceUtilityModule());
        install(new DatabaseHealthStatusDaoAggregator());

        install(new StandardServicesModule());
        install(new UnscopedServicesModule());
        install(new ScopedServicesModule(scope));

        install(new DeprecatedServicesModule());

        install(new Web3jBlockchainSupportModule());
        install(new OmniBlockchainServicesUtilityModule());

        install(new EvmInvokerModule());
        install(new FlowInvokerModule());

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
        expose(EvmSmartContractInvocationService.class);
        expose(FlowSmartContractInvocationService.class);

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
        expose(EvmSmartContractInvocationService.class).annotatedWith(Unscoped.class);
        expose(FlowSmartContractInvocationService.class).annotatedWith(Unscoped.class);

    }

}
