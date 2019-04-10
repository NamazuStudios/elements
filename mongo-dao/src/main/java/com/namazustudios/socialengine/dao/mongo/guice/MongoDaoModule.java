package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.*;
import com.namazustudios.socialengine.dao.mongo.application.*;
import com.namazustudios.socialengine.dao.mongo.gameon.MongoGameOnRegistrationDao;
import com.namazustudios.socialengine.dao.mongo.gameon.MongoGameOnSessionDao;
import com.namazustudios.socialengine.dao.mongo.match.MongoMatchDao;
import com.namazustudios.socialengine.dao.mongo.provider.MongoAdvancedDatastoreProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDozerMapperProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoMatchmakerFunctionProvider;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;

import java.security.MessageDigest;
import java.util.function.Function;

/**
 * Configures any Mongo-specific system properties.
 *
 * This is intentionally separated from the {@link MongoSearchModule} as it may or
 * may not be desirable to use the mongo search, depending on the application.
 *
 * The only dependency this module has but does not not provide is one of
 * an instance of {@link ObjectIndex}.
 *
 * Created by patricktwohig on 4/3/15.
 */
public class MongoDaoModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(UserDao.class).to(MongoUserDao.class);
        bind(ProfileDao.class).to(MongoProfileDao.class);
        bind(FacebookUserDao.class).to(MongoFacebookUserDao.class);
        bind(SocialCampaignDao.class).to(MongoSocialCampaignDao.class);
        bind(ShortLinkDao.class).to(MongoShortLinkDao.class);
        bind(ApplicationDao.class).to(MongoApplicationDao.class);
        bind(ApplicationConfigurationDao.class).to(MongoApplicationConfigurationDao.class);
        bind(IosApplicationConfigurationDao.class).to(MongoIosApplicationConfigurationDao.class);
        bind(GooglePlayApplicationConfigurationDao.class).to(MongoGooglePlayApplicationConfigurationDao.class);
        bind(FacebookApplicationConfigurationDao.class).to(MongoFacebookApplicationConfigurationDao.class);
        bind(MatchmakingApplicationConfigurationDao.class).to(MongoMatchmakingApplicationConfigurationDao.class);
        bind(FirebaseApplicationConfigurationDao.class).to(MongoFirebaseApplicationConfigurationDao.class);
        bind(GameOnApplicationConfigurationDao.class).to(MongoGameOnApplicationConfigurationDao.class);
        bind(GameOnRegistrationDao.class).to(MongoGameOnRegistrationDao.class);
        bind(GameOnSessionDao.class).to(MongoGameOnSessionDao.class);
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

        bind(Datastore.class)
            .toProvider(MongoAdvancedDatastoreProvider.class)
            .asEagerSingleton();

        bind(AdvancedDatastore.class)
            .toProvider(MongoAdvancedDatastoreProvider.class)
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
        expose(SocialCampaignDao.class);
        expose(ShortLinkDao.class);
        expose(ApplicationDao.class);
        expose(ApplicationConfigurationDao.class);
        expose(IosApplicationConfigurationDao.class);
        expose(GooglePlayApplicationConfigurationDao.class);
        expose(FacebookApplicationConfigurationDao.class);
        expose(MatchmakingApplicationConfigurationDao.class);
        expose(FirebaseApplicationConfigurationDao.class);
        expose(GameOnApplicationConfigurationDao.class);
        expose(MatchDao.class);
        expose(FCMRegistrationDao.class);
        expose(SessionDao.class);
        expose(LeaderboardDao.class);
        expose(ScoreDao.class);
        expose(RankDao.class);
        expose(FriendDao.class);
        expose(FacebookFriendDao.class);
        expose(GameOnRegistrationDao.class);
        expose(GameOnSessionDao.class);
        expose(ItemDao.class);
        expose(InventoryItemDao.class);
        expose(MissionDao.class);
        expose(ProgressDao.class);
        expose(RewardIssuanceDao.class);
        expose(AppleIapReceiptDao.class);
        expose(GooglePlayIapReceiptDao.class);
    }
}
