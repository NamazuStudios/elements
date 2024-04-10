package dev.getelements.elements.dao.mongo.provider;

import dev.getelements.elements.dao.mongo.converter.*;
import dev.getelements.elements.dao.mongo.model.*;
import dev.getelements.elements.dao.mongo.model.application.*;
import dev.getelements.elements.dao.mongo.model.blockchain.*;
import dev.getelements.elements.dao.mongo.model.formidium.MongoFormidiumInvestor;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.match.MongoMatch;
import dev.getelements.elements.dao.mongo.model.match.MongoMatchSnapshot;
import dev.getelements.elements.dao.mongo.model.mission.*;
import dev.getelements.elements.dao.mongo.model.savedata.MongoSaveDataDocument;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpecProperty;
import dev.getelements.elements.dao.mongo.model.score.MongoScore;
import dev.getelements.elements.model.Deployment;
import dev.getelements.elements.model.application.*;
import dev.getelements.elements.model.blockchain.ElementsSmartContract;
import dev.getelements.elements.model.blockchain.contract.SmartContract;
import dev.getelements.elements.model.blockchain.wallet.Vault;
import dev.getelements.elements.model.blockchain.wallet.Wallet;
import dev.getelements.elements.model.formidium.FormidiumInvestor;
import dev.getelements.elements.model.friend.Friend;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.inventory.InventoryItem;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.model.leaderboard.RankRow;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.model.match.Match;
import dev.getelements.elements.model.mission.*;
import dev.getelements.elements.model.notification.FCMRegistration;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.reward.Reward;
import dev.getelements.elements.model.reward.RewardIssuance;
import dev.getelements.elements.model.savedata.SaveDataDocument;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.user.User;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;

import javax.inject.Provider;

import static org.dozer.loader.api.FieldsMappingOptions.*;

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

            mapping(RankRow.class, Rank.class)
                .fields("id", "score.id")
                .fields("pointValue", "score.pointValue")
                .fields("scoreUnits", "score.scoreUnits")
                .fields("creationTimestamp", "score.creationTimestamp")
                .fields("leaderboardEpoch", "score.leaderboardEpoch")
                .fields("profileId", "score.profile.id")
                .fields("profileDisplayName", "score.profile.displayName")
                .fields("profileImageUrl", "score.profile.imageUrl")
                .fields("lastLogin", "score.profile.lastLogin");

            mapping(Friend.class, MongoFriendship.class)
                .fields("id", "objectId", customConverter(MongoFriendIdConverter.class));

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

            mapping(Schedule.class, MongoSchedule.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(ScheduleEvent.class, MongoScheduleEvent.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(Deployment.class, MongoDeployment.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(ElementsSmartContract.class, MongoNeoSmartContract.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(ElementsSmartContract.class, MongoBscSmartContract.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(MetadataSpec.class, MongoMetadataSpec.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields("properties","properties", hintA(MetadataSpecProperty.class), hintB(MongoMetadataSpecProperty.class));

            mapping(MetadataSpecProperty.class, MongoMetadataSpecProperty.class)
                    .fields("properties", "properties", hintA(MetadataSpecProperty.class), hintB(MongoMetadataSpecProperty.class));

            mapping(SaveDataDocument.class, MongoSaveDataDocument.class)
                    .fields("id", "saveDataDocumentId", customConverter(MongoHexableIdConverter.class))
                    .fields("slot", "saveDataDocumentId.slot", customConverter(IdentityConverter.class))
                    .fields("version", "version", customConverter(HexStringByteConverter.class));

            mapping(DistinctInventoryItem.class, MongoDistinctInventoryItem.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields("metadata","metadata", customConverter(IdentityConverter.class));

            mapping(FormidiumInvestor.class, MongoFormidiumInvestor.class)
                    .fields("id", "id", customConverter(MongoHexableIdConverter.class));

            mapping(Wallet.class, MongoWallet.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(SmartContract.class, MongoSmartContract.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields(
                            "addresses",
                            "addresses",
                            useMapId("addresses"),
                            customConverter(MongoSmartContractAddressesConverter.class)
                    )
            ;

            mapping(Vault.class, MongoVault.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
