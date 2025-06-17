package dev.getelements.elements.dao.mongo.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import dev.getelements.elements.dao.mongo.*;
import dev.getelements.elements.dao.mongo.application.MongoApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.auth.MongoAuthSchemeDao;
import dev.getelements.elements.dao.mongo.auth.MongoOAuth2AuthSchemeDao;
import dev.getelements.elements.dao.mongo.auth.MongoOidcAuthSchemeDao;
import dev.getelements.elements.dao.mongo.blockchain.MongoSmartContractDao;
import dev.getelements.elements.dao.mongo.blockchain.MongoVaultDao;
import dev.getelements.elements.dao.mongo.blockchain.MongoWalletDao;
import dev.getelements.elements.dao.mongo.goods.MongoDistinctInventoryItemDao;
import dev.getelements.elements.dao.mongo.goods.MongoDistinctInventoryItemIndexable;
import dev.getelements.elements.dao.mongo.goods.MongoInventoryItemDao;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.dao.mongo.health.MongoDatabaseHealthStatusDao;
import dev.getelements.elements.dao.mongo.largeobject.MongoLargeObjectDao;
import dev.getelements.elements.dao.mongo.match.MongoFIFOMatchmaker;
import dev.getelements.elements.dao.mongo.match.MongoMatchDao;
import dev.getelements.elements.dao.mongo.match.MongoMultiMatchDao;
import dev.getelements.elements.dao.mongo.mission.*;
import dev.getelements.elements.dao.mongo.provider.MongoDatastoreProvider;
import dev.getelements.elements.dao.mongo.provider.MongoDozerMapperProvider;
import dev.getelements.elements.dao.mongo.query.*;
import dev.getelements.elements.dao.mongo.savedata.MongoSaveDataDocumentDao;
import dev.getelements.elements.dao.mongo.schema.MongoMetadataSpecDao;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.index.IndexableType;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;

import java.security.MessageDigest;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static dev.getelements.elements.sdk.model.index.IndexableType.DISTINCT_INVENTORY_ITEM;

/**
 * Configures any Mongo-specific system properties.
 *
 * Created by patricktwohig on 4/3/15.
 */
public class MongoDaoModule extends PrivateModule {

    @Override
    protected void configure() {

        bindMapper();
        bindDatastore();
        bindTransaction();

        bind(ObjectMapper.class).asEagerSingleton();

        bind(UserDao.class).to(MongoUserDao.class);
        bind(UserUidDao.class).to(MongoUserUidDao.class);
        bind(ProfileDao.class).to(MongoProfileDao.class);
        bind(ApplicationDao.class).to(MongoApplicationDao.class);
        bind(ApplicationConfigurationDao.class).to(MongoApplicationConfigurationDao.class);
        bind(MatchDao.class).to(MongoMatchDao.class);
        bind(MultiMatchDao.class).to(MongoMultiMatchDao.class);
        bind(SessionDao.class).to(MongoSessionDao.class);
        bind(FCMRegistrationDao.class).to(MongoFCMRegistrationDao.class);
        bind(LeaderboardDao.class).to(MongoLeaderboardDao.class);
        bind(ScoreDao.class).to(MongoScoreDao.class);
        bind(RankDao.class).to(MongoRankDao.class);
        bind(FriendDao.class).to(MongoFriendDao.class);
        bind(ItemDao.class).to(MongoItemDao.class);
        bind(InventoryItemDao.class).to(MongoInventoryItemDao.class);
        bind(MissionDao.class).to(MongoMissionDao.class);
        bind(ProgressDao.class).to(MongoProgressDao.class);
        bind(RewardIssuanceDao.class).to(MongoRewardIssuanceDao.class);
        bind(AppleIapReceiptDao.class).to(MongoAppleIapReceiptDao.class);
        bind(GooglePlayIapReceiptDao.class).to(MongoGooglePlayIapReceiptDao.class);
        bind(FollowerDao.class).to(MongoFollowerDao.class);
        bind(TokensWithExpirationDao.class).to(MongoTokensWithExpirationDao.class);
        bind(DeploymentDao.class).to(MongoDeploymentDao.class);
        bind(DatabaseHealthStatusDao.class).to(MongoDatabaseHealthStatusDao.class);
        bind(MetadataSpecDao.class).to(MongoMetadataSpecDao.class);
        bind(SaveDataDocumentDao.class).to(MongoSaveDataDocumentDao.class);
        bind(AuthSchemeDao.class).to(MongoAuthSchemeDao.class);
        bind(OidcAuthSchemeDao.class).to(MongoOidcAuthSchemeDao.class);
        bind(OAuth2AuthSchemeDao.class).to(MongoOAuth2AuthSchemeDao.class);
        bind(DistinctInventoryItemDao.class).to(MongoDistinctInventoryItemDao.class);
        bind(WalletDao.class).to(MongoWalletDao.class);
        bind(SmartContractDao.class).to(MongoSmartContractDao.class);
        bind(VaultDao.class).to(MongoVaultDao.class);
        bind(LargeObjectDao.class).to(MongoLargeObjectDao.class);
        bind(ScheduleDao.class).to(MongoScheduleDao.class);
        bind(ScheduleEventDao.class).to(MongoScheduleEventDao.class);
        bind(ScheduleProgressDao.class).to(MongoScheduleProgressDao.class);
        bind(Matchmaker.class).to(MongoFIFOMatchmaker.class);

        bind(MessageDigest.class)
                .annotatedWith(Names.named(Constants.PASSWORD_DIGEST))
                .toProvider(PasswordDigestProvider.class);

        bind(BooleanQueryParser.class)
                .to(SidhantAggarwalBooleanQueryParser.class);

        bind(IndexDao.class)
                .to(MongoIndexDao.class);

        bind(IndexDao.Indexer.class)
                .to(MongoIndexer.class);

        final var booleanQueryOperatorSet = Multibinder.newSetBinder(binder(), BooleanQueryOperator.class);
        booleanQueryOperatorSet.addBinding().to(NameBooleanQueryOperator.class).asEagerSingleton();
        booleanQueryOperatorSet.addBinding().to(ReferenceBooleanQueryOperator.class).asEagerSingleton();

        final var indexableByType = newMapBinder(binder(), IndexableType.class, Indexable.class);
        indexableByType.addBinding(DISTINCT_INVENTORY_ITEM).to(MongoDistinctInventoryItemIndexable.class);

        expose(Datastore.class);
        expose(Transaction.class);

        expose(IndexDao.class);
        expose(UserDao.class);
        expose(UserUidDao.class);
        expose(ProfileDao.class);
        expose(ApplicationDao.class);
        expose(ApplicationConfigurationDao.class);
        expose(MatchDao.class);
        expose(MultiMatchDao.class);
        expose(FCMRegistrationDao.class);
        expose(SessionDao.class);
        expose(LeaderboardDao.class);
        expose(ScoreDao.class);
        expose(RankDao.class);
        expose(FriendDao.class);
        expose(ItemDao.class);
        expose(InventoryItemDao.class);
        expose(MissionDao.class);
        expose(ProgressDao.class);
        expose(RewardIssuanceDao.class);
        expose(AppleIapReceiptDao.class);
        expose(GooglePlayIapReceiptDao.class);
        expose(FollowerDao.class);
        expose(TokensWithExpirationDao.class);
        expose(DeploymentDao.class);
        expose(DatabaseHealthStatusDao.class);
        expose(MetadataSpecDao.class);
        expose(SaveDataDocumentDao.class);
        expose(AuthSchemeDao.class);
        expose(OidcAuthSchemeDao.class);
        expose(OAuth2AuthSchemeDao.class);
        expose(DistinctInventoryItemDao.class);
        expose(WalletDao.class);
        expose(SmartContractDao.class);
        expose(VaultDao.class);
        expose(LargeObjectDao.class);
        expose(ScheduleDao.class);
        expose(ScheduleEventDao.class);
        expose(ScheduleProgressDao.class);

    }

    protected void bindMapper() {
        bind(MapperRegistry.class)
                .toProvider(MongoDozerMapperProvider.class)
                .asEagerSingleton();
    }

    protected void bindDatastore() {
        bind(Datastore.class)
                .toProvider(MongoDatastoreProvider.class)
                .asEagerSingleton();
    }

    protected void bindTransaction() {
        bind(Transaction.class).toProvider(MongoTransactionProvider.class);
    }

}
