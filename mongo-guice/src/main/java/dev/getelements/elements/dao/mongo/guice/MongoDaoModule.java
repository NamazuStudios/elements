package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import dev.getelements.elements.Constants;
import dev.getelements.elements.dao.*;
import dev.getelements.elements.dao.mongo.*;
import dev.getelements.elements.dao.mongo.applesignin.MongoAppleSignInSessionDao;
import dev.getelements.elements.dao.mongo.applesignin.MongoAppleSignInUserDao;
import dev.getelements.elements.dao.mongo.application.*;
import dev.getelements.elements.dao.mongo.auth.MongoAuthSchemeDao;
import dev.getelements.elements.dao.mongo.auth.MongoCustomAuthUserDao;
import dev.getelements.elements.dao.mongo.blockchain.MongoSmartContractDao;
import dev.getelements.elements.dao.mongo.blockchain.MongoVaultDao;
import dev.getelements.elements.dao.mongo.blockchain.MongoWalletDao;
import dev.getelements.elements.dao.mongo.formidium.MongoFormidiumInvestorDao;
import dev.getelements.elements.dao.mongo.goods.MongoDistinctInventoryItemDao;
import dev.getelements.elements.dao.mongo.goods.MongoInventoryItemDao;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.dao.mongo.health.MongoDatabaseHealthStatusDao;
import dev.getelements.elements.dao.mongo.largeobject.MongoLargeObjectDao;
import dev.getelements.elements.dao.mongo.match.MongoMatchDao;
import dev.getelements.elements.dao.mongo.provider.MongoDatastoreProvider;
import dev.getelements.elements.dao.mongo.provider.MongoDozerMapperProvider;
import dev.getelements.elements.dao.mongo.provider.MongoMatchmakerFunctionProvider;
import dev.getelements.elements.dao.mongo.savedata.MongoSaveDataDocumentDao;
import dev.getelements.elements.dao.mongo.schema.MongoMetadataSpecDao;
import dev.getelements.elements.model.match.MatchingAlgorithm;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import java.security.MessageDigest;
import java.util.function.Function;

/**
 * Configures any Mongo-specific system properties.
 *
 * Created by patricktwohig on 4/3/15.
 */
public class MongoDaoModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(UserDao.class).to(MongoUserDao.class);
        bind(ProfileDao.class).to(MongoProfileDao.class);
        bind(FacebookUserDao.class).to(MongoFacebookUserDao.class);
        bind(ShortLinkDao.class).to(MongoShortLinkDao.class);
        bind(ApplicationDao.class).to(MongoApplicationDao.class);
        bind(ApplicationConfigurationDao.class).to(MongoApplicationConfigurationDao.class);
        bind(IosApplicationConfigurationDao.class).to(MongoIosApplicationConfigurationDao.class);
        bind(GooglePlayApplicationConfigurationDao.class).to(MongoGooglePlayApplicationConfigurationDao.class);
        bind(FacebookApplicationConfigurationDao.class).to(MongoFacebookApplicationConfigurationDao.class);
        bind(MatchmakingApplicationConfigurationDao.class).to(MongoMatchmakingApplicationConfigurationDao.class);
        bind(FirebaseApplicationConfigurationDao.class).to(MongoFirebaseApplicationConfigurationDao.class);
        bind(MatchDao.class).to(MongoMatchDao.class);
        bind(SessionDao.class).to(MongoSessionDao.class);
        bind(FCMRegistrationDao.class).to(MongoFCMRegistrationDao.class);
        bind(LeaderboardDao.class).to(MongoLeaderboardDao.class);
        bind(ScoreDao.class).to(MongoScoreDao.class);
        bind(RankDao.class).to(MongoRankDao.class);
        bind(FriendDao.class).to(MongoFriendDao.class);
        bind(FacebookFriendDao.class).to(MongoFacebookFriendDao.class);
        bind(ItemDao.class).to(MongoItemDao.class);
        bind(InventoryItemDao.class).to(MongoInventoryItemDao.class);
        bind(MissionDao.class).to(MongoMissionDao.class);
        bind(ProgressDao.class).to(MongoProgressDao.class);
        bind(RewardIssuanceDao.class).to(MongoRewardIssuanceDao.class);
        bind(AppleIapReceiptDao.class).to(MongoAppleIapReceiptDao.class);
        bind(GooglePlayIapReceiptDao.class).to(MongoGooglePlayIapReceiptDao.class);
        bind(FirebaseUserDao.class).to(MongoFirebaseUserDao.class);
        bind(AppleSignInUserDao.class).to(MongoAppleSignInUserDao.class);
        bind(AppleSignInSessionDao.class).to(MongoAppleSignInSessionDao.class);
        bind(FollowerDao.class).to(MongoFollowerDao.class);
        bind(TokensWithExpirationDao.class).to(MongoTokensWithExpirationDao.class);
        bind(DeploymentDao.class).to(MongoDeploymentDao.class);
        bind(DatabaseHealthStatusDao.class).to(MongoDatabaseHealthStatusDao.class);
        bind(MetadataSpecDao.class).to(MongoMetadataSpecDao.class);
        bind(SaveDataDocumentDao.class).to(MongoSaveDataDocumentDao.class);
        bind(AuthSchemeDao.class).to(MongoAuthSchemeDao.class);
        bind(CustomAuthUserDao.class).to(MongoCustomAuthUserDao.class);
        bind(DistinctInventoryItemDao.class).to(MongoDistinctInventoryItemDao.class);
        bind(FormidiumInvestorDao.class).to(MongoFormidiumInvestorDao.class);
        bind(WalletDao.class).to(MongoWalletDao.class);
        bind(SmartContractDao.class).to(MongoSmartContractDao.class);
        bind(VaultDao.class).to(MongoVaultDao.class);
        bind(LargeObjectDao.class).to(MongoLargeObjectDao.class);

        bind(Datastore.class)
            .toProvider(MongoDatastoreProvider.class)
            .asEagerSingleton();

        bind(MessageDigest.class)
            .annotatedWith(Names.named(Constants.PASSWORD_DIGEST))
            .toProvider(PasswordDigestProvider.class);

        bind(Mapper.class)
            .toProvider(MongoDozerMapperProvider.class)
            .asEagerSingleton();

        bind(new TypeLiteral<Function<MatchingAlgorithm, Matchmaker>>(){})
            .toProvider(MongoMatchmakerFunctionProvider.class);

        expose(UserDao.class);
        expose(ProfileDao.class);
        expose(FacebookUserDao.class);
        expose(ShortLinkDao.class);
        expose(ApplicationDao.class);
        expose(ApplicationConfigurationDao.class);
        expose(IosApplicationConfigurationDao.class);
        expose(GooglePlayApplicationConfigurationDao.class);
        expose(FacebookApplicationConfigurationDao.class);
        expose(MatchmakingApplicationConfigurationDao.class);
        expose(FirebaseApplicationConfigurationDao.class);
        expose(FirebaseUserDao.class);
        expose(MatchDao.class);
        expose(FCMRegistrationDao.class);
        expose(SessionDao.class);
        expose(LeaderboardDao.class);
        expose(ScoreDao.class);
        expose(RankDao.class);
        expose(FriendDao.class);
        expose(FacebookFriendDao.class);
        expose(ItemDao.class);
        expose(InventoryItemDao.class);
        expose(MissionDao.class);
        expose(ProgressDao.class);
        expose(RewardIssuanceDao.class);
        expose(AppleIapReceiptDao.class);
        expose(GooglePlayIapReceiptDao.class);
        expose(AppleSignInUserDao.class);
        expose(AppleSignInSessionDao.class);
        expose(FollowerDao.class);
        expose(TokensWithExpirationDao.class);
        expose(DeploymentDao.class);
        expose(DatabaseHealthStatusDao.class);
        expose(MetadataSpecDao.class);
        expose(SaveDataDocumentDao.class);
        expose(AuthSchemeDao.class);
        expose(CustomAuthUserDao.class);
        expose(DistinctInventoryItemDao.class);
        expose(FormidiumInvestorDao.class);
        expose(WalletDao.class);
        expose(SmartContractDao.class);
        expose(VaultDao.class);
        expose(LargeObjectDao.class);

    }

}
