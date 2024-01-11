package dev.getelements.elements.service;

import com.google.inject.*;
import dev.getelements.elements.annotation.FacebookPermission;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.*;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.remote.ControlClient;
import dev.getelements.elements.rt.remote.InstanceDiscoveryService;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;
import dev.getelements.elements.security.PasswordGenerator;
import dev.getelements.elements.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.service.blockchain.crypto.VaultCryptoUtilities;
import dev.getelements.elements.service.blockchain.crypto.WalletAccountFactory;
import dev.getelements.elements.service.blockchain.crypto.WalletCryptoUtilities;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.firebase.FirebaseAppFactory;
import dev.getelements.elements.service.guice.*;
import dev.getelements.elements.service.profile.ProfileImageObjectUtils;
import dev.getelements.elements.service.util.CryptoKeyPairUtility;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validator;
import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static dev.getelements.elements.model.user.User.Level.UNPRIVILEGED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class MockServiceTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MockServiceTestModule.class);

    @Override
    protected void configure() {

        install(new StandardServicesModule());
        install(new UnscopedServicesModule());
        install(new ScopedServicesModule(TEST_SCOPE));

        install(new EvmInvokerModule());
        install(new FlowInvokerModule());
        install(new NearInvokerModule());

        install(new ConfigurationModule(new DefaultConfigurationSupplier()));

        bind(Mapper.class).toProvider(ServicesDozerMapperProvider.class);

        bind(Validator.class).toInstance(mock(Validator.class));
        bind(Attributes.class).toInstance(mock(Attributes.class));
        bind(ControlClient.class).toInstance(mock(ControlClient.class));
        bind(LargeObjectBucket.class).toInstance(mock(LargeObjectBucket.class));
        bind(Context.Factory.class).toInstance(mock(Context.Factory.class));
        bind(InstanceDiscoveryService.class).toInstance(mock(InstanceDiscoveryService.class));
        bind(RemoteInvokerRegistry.class).toInstance(mock(RemoteInvokerRegistry.class));
        bind(PasswordGenerator.class).toInstance(mock(PasswordGenerator.class));
        bind(VaultCryptoUtilities.class).toInstance(mock(VaultCryptoUtilities.class));
        bind(WalletCryptoUtilities.class).toInstance(mock(WalletCryptoUtilities.class));
        bind(WalletAccountFactory.class).toInstance(mock(WalletAccountFactory.class));
        bind(Client.class).toInstance(mock(Client.class));
        bind(ScopedInvoker.Factory.class).toInstance(mock(ScopedInvoker.Factory.class));
        bind(FirebaseAppFactory.class).toInstance(mock(FirebaseAppFactory.class));
        bind(CryptoKeyPairUtility.class).toInstance(mock(CryptoKeyPairUtility.class));
        bind(MatchServiceUtils.class).toInstance(mock(MatchServiceUtils.class));
        bind(AppleIapVerifyReceiptInvoker.Builder.class).toInstance(mock(AppleIapVerifyReceiptInvoker.Builder.class));
        bind(FacebookFriendCache.class).toInstance(mock(FacebookFriendCache.class));
        bind(TopicService.class).toInstance(mock(TopicService.class));

        bind(UserDao.class).toInstance(mock(UserDao.class));
        bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
        bind(FacebookUserDao.class).toInstance(mock(FacebookUserDao.class));
        bind(ShortLinkDao.class).toInstance(mock(ShortLinkDao.class));
        bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
        bind(ApplicationConfigurationDao.class).toInstance(mock(ApplicationConfigurationDao.class));
        bind(IosApplicationConfigurationDao.class).toInstance(mock(IosApplicationConfigurationDao.class));
        bind(GooglePlayApplicationConfigurationDao.class).toInstance(mock(GooglePlayApplicationConfigurationDao.class));
        bind(FacebookApplicationConfigurationDao.class).toInstance(mock(FacebookApplicationConfigurationDao.class));
        bind(MatchmakingApplicationConfigurationDao.class).toInstance(mock(MatchmakingApplicationConfigurationDao.class));
        bind(FirebaseApplicationConfigurationDao.class).toInstance(mock(FirebaseApplicationConfigurationDao.class));
        bind(MatchDao.class).toInstance(mock(MatchDao.class));
        bind(SessionDao.class).toInstance(mock(SessionDao.class));
        bind(FCMRegistrationDao.class).toInstance(mock(FCMRegistrationDao.class));
        bind(LeaderboardDao.class).toInstance(mock(LeaderboardDao.class));
        bind(ScoreDao.class).toInstance(mock(ScoreDao.class));
        bind(RankDao.class).toInstance(mock(RankDao.class));
        bind(FriendDao.class).toInstance(mock(FriendDao.class));
        bind(FacebookFriendDao.class).toInstance(mock(FacebookFriendDao.class));
        bind(ItemDao.class).toInstance(mock(ItemDao.class));
        bind(InventoryItemDao.class).toInstance(mock(InventoryItemDao.class));
        bind(MissionDao.class).toInstance(mock(MissionDao.class));
        bind(ProgressDao.class).toInstance(mock(ProgressDao.class));
        bind(RewardIssuanceDao.class).toInstance(mock(RewardIssuanceDao.class));
        bind(AppleIapReceiptDao.class).toInstance(mock(AppleIapReceiptDao.class));
        bind(GooglePlayIapReceiptDao.class).toInstance(mock(GooglePlayIapReceiptDao.class));
        bind(FirebaseUserDao.class).toInstance(mock(FirebaseUserDao.class));
        bind(AppleSignInUserDao.class).toInstance(mock(AppleSignInUserDao.class));
        bind(AppleSignInSessionDao.class).toInstance(mock(AppleSignInSessionDao.class));
        bind(GoogleSignInUserDao.class).toInstance(mock(GoogleSignInUserDao.class));
        bind(GoogleSignInSessionDao.class).toInstance(mock(GoogleSignInSessionDao.class));
        bind(FollowerDao.class).toInstance(mock(FollowerDao.class));
        bind(DeploymentDao.class).toInstance(mock(DeploymentDao.class));
        bind(DatabaseHealthStatusDao.class).toInstance(mock(DatabaseHealthStatusDao.class));
        bind(MetadataSpecDao.class).toInstance(mock(MetadataSpecDao.class));
        bind(SaveDataDocumentDao.class).toInstance(mock(SaveDataDocumentDao.class));
        bind(AuthSchemeDao.class).toInstance(mock(AuthSchemeDao.class));
        bind(CustomAuthUserDao.class).toInstance(mock(CustomAuthUserDao.class));
        bind(DistinctInventoryItemDao.class).toInstance(mock(DistinctInventoryItemDao.class));
        bind(FormidiumInvestorDao.class).toInstance(mock(FormidiumInvestorDao.class));
        bind(WalletDao.class).toInstance(mock(WalletDao.class));
        bind(SmartContractDao.class).toInstance(mock(SmartContractDao.class));
        bind(VaultDao.class).toInstance(mock(VaultDao.class));
        bind(LargeObjectDao.class).toInstance(mock(LargeObjectDao.class));
        bind(IndexDao.class).toInstance(mock(IndexDao.class));

        final var databaseHealthStatusDaos = newSetBinder(binder(), DatabaseHealthStatusDao.class);
        databaseHealthStatusDaos.addBinding().toInstance(mock(DatabaseHealthStatusDao.class));

        final var userSpy = spy(User.class);
        userSpy.setLevel(UNPRIVILEGED);
        bind(User.class).toInstance(userSpy);

        bind(new TypeLiteral<Optional<Profile>>(){}).toInstance(Optional.empty());
        bind(new TypeLiteral<Supplier<Profile>>(){}).toInstance(mock(Supplier.class));

        bind(new TypeLiteral<Supplier<List<FacebookPermission>>>(){}).to(FacebookBuiltinPermissionsSupplier.class);

    }

    private static final ThreadLocal<Boolean> in = new ThreadLocal<>();

    public static void enter() {
        in.set(true);
    }

    public static void exit() {
        in.set(false);
    }

    public static final Scope TEST_SCOPE = new Scope() {


        @Override
        public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
            return () -> {

                final var in = MockServiceTestModule.in.get();

                if (in == null || !in) {
                    throw new IllegalStateException("Not in scope.");
                }

                return unscoped.get();

            };
        }

    };

    public static class EmptyAttributesProvider implements javax.inject.Provider<Attributes> {
        @Override
        public Attributes get() {
            return Attributes.emptyAttributes();
        }
    }
}
