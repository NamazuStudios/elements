package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.dao.mongo.converter.*;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.dao.mongo.model.application.*;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.*;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnRegistration;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnSession;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnSessionId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoDistinctInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.match.MongoMatch;
import com.namazustudios.socialengine.dao.mongo.model.match.MongoMatchSnapshot;
import com.namazustudios.socialengine.dao.mongo.model.mission.*;
import com.namazustudios.socialengine.dao.mongo.model.savedata.MongoSaveDataDocument;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.application.*;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.model.blockchain.neo.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.template.TemplateTab;
import com.namazustudios.socialengine.model.blockchain.template.TemplateTabField;
import com.namazustudios.socialengine.model.blockchain.template.TokenTemplate;
import com.namazustudios.socialengine.model.friend.Friend;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.inventory.DistinctInventoryItem;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.mission.ProgressMissionInfo;
import com.namazustudios.socialengine.model.mission.Step;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.reward.Reward;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.user.User;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;

import javax.inject.Provider;

import static org.dozer.loader.api.FieldsMappingOptions.customConverter;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDozerMapperProvider implements Provider<Mapper> {

    @Override
    public Mapper get() {

        final BeanMappingBuilder beanMappingBuilder = new BeanMappingBuilder() {
            @Override
            protected void configure() {

            mapping(User.class, MongoUser.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(ApplicationConfiguration.class, MongoApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("parent.id", "parent.objectId", customConverter(ObjectIdConverter.class))
                .fields("uniqueIdentifier", "uniqueIdentifier");

            mapping(PSNApplicationConfiguration.class, MongoPSNApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("npIdentifier", "uniqueIdentifier");

            mapping(IosApplicationConfiguration.class, MongoIosApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("applicationId","uniqueIdentifier");

            mapping(GooglePlayApplicationConfiguration.class, MongoGooglePlayApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("applicationId","uniqueIdentifier");

            mapping(FacebookApplicationConfiguration.class, MongoFacebookApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("applicationId","uniqueIdentifier");

            mapping(MatchmakingApplicationConfiguration.class, MongoMatchmakingApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("scheme", "uniqueIdentifier");

            mapping(FirebaseApplicationConfiguration.class, MongoFirebaseApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("projectId", "uniqueIdentifier");

            mapping(GameOnApplicationConfiguration.class, MongoGameOnApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("gameId", "uniqueIdentifier");

            mapping(Profile.class, MongoProfile.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("application.id", "application.objectId", customConverter(ObjectIdConverter.class))
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(Match.class, MongoMatch.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(MongoMatch.class, MongoMatchSnapshot.class)
                .fields("player.objectId", "player.objectId", customConverter(ObjectIdConverter.class))
                .fields("opponent.objectId", "opponent.objectId", customConverter(ObjectIdConverter.class));

            mapping(Application.class, MongoApplication.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(FCMRegistration.class, MongoFCMRegistration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("profile.id", "profile.objectId", customConverter(ObjectIdConverter.class));

            mapping(Leaderboard.class, MongoLeaderboard.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(Score.class, MongoScore.class)
                .fields("id", "objectId", customConverter(MongoScoreIdConverter.class))
                .fields("scoreUnits", "leaderboard.scoreUnits");

            mapping(Friend.class, MongoFriendship.class)
                .fields("id", "objectId", customConverter(MongoFriendIdConverter.class));

            mapping(GameOnRegistration.class, MongoGameOnRegistration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(GameOnSession.class, MongoGameOnSession.class)
                .fields("id", "objectId", customConverter(MongoGameOnSessionId.Converter.class))
                .fields("deviceOSType", "objectId.deviceOSType");

            mapping(Item.class, MongoItem.class)
                .fields("id","objectId", customConverter(ObjectIdConverter.class))
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(InventoryItem.class, MongoInventoryItem.class)
                .fields("id","objectId", customConverter(MongoInventoryItemIdConverter.class))
                .fields("priority", "objectId.priority")
                .fields("user.id", "objectId.userObjectId", customConverter(ObjectIdConverter.class))
                .fields("item.id", "objectId.itemObjectId", customConverter(ObjectIdConverter.class));

            mapping(Mission.class, MongoMission.class)
                .fields("id","objectId", customConverter(ObjectIdConverter.class))
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(Progress.class, MongoProgress.class)
                .fields("id","objectId", customConverter(MongoProgressIdConverter.class))
                .fields("profile.id", "objectId.profileId", customConverter(ObjectIdConverter.class))
                .fields("mission.id", "objectId.missionId", customConverter(ObjectIdConverter.class));

            mapping(RewardIssuance.class, MongoRewardIssuance.class)
                .fields("id","objectId", customConverter(MongoRewardIssuanceIdConverter.class))
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(Reward.class, MongoReward.class)
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(ProgressMissionInfo.class, MongoProgressMissionInfo.class)
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(Step.class, MongoStep.class)
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(ProductBundle.class, MongoProductBundle.class)
                .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(Deployment.class, MongoDeployment.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(NeoWallet.class, MongoNeoWallet.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields("wallet", "wallet", customConverter(MongoNeoWalletConverter.class));

            mapping(NeoToken.class, MongoNeoToken.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(TokenTemplate.class, MongoTokenTemplate.class)
                        .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(TemplateTab.class, MongoTemplateTab.class).fields("fields","fields");

            mapping(TemplateTabField.class, MongoTemplateTabField.class).fields("fieldType","fieldType");

            mapping(SaveDataDocument.class, MongoSaveDataDocument.class)
                .fields("id", "saveDataDocumentId", customConverter(MongoHexableIdConverter.class))
                .fields("slot", "saveDataDocumentId.slot", customConverter(IdentityConverter.class))
                .fields("version", "version", customConverter(HexStringByteConverter.class));

            mapping(DistinctInventoryItem.class, MongoDistinctInventoryItem.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("metadata","metadata", customConverter(IdentityConverter.class));;

            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
