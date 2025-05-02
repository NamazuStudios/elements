package dev.getelements.elements.dao.mongo.mapper;


import dev.getelements.elements.dao.mongo.model.*;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoApplicationConfiguration;
import dev.getelements.elements.dao.mongo.model.auth.MongoAuthScheme;
import dev.getelements.elements.dao.mongo.model.blockchain.*;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexMetadata;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlanStep;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.dao.mongo.model.match.MongoMatch;
import dev.getelements.elements.dao.mongo.model.mission.*;
import dev.getelements.elements.dao.mongo.model.savedata.MongoSaveDataDocument;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.dao.mongo.model.score.MongoScore;
import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.auth.AuthScheme;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.ElementsSmartContract;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContract;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContractAddress;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.blockchain.wallet.VaultKey;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.index.IndexMetadata;
import dev.getelements.elements.sdk.model.index.IndexPlanStep;
import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.Rank;
import dev.getelements.elements.sdk.model.leaderboard.RankRow;
import dev.getelements.elements.sdk.model.leaderboard.Score;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.notification.FCMRegistration;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.Reward;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.savedata.SaveDataDocument;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.user.User;
import org.bson.Document;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Interface to provide mappings for MongoTypes
 *
 * @deprecated This is a bit of a "god type" and should be broken up into smaller mappers.
 */
@Deprecated
@Mapper(uses = PropertyConverters.class)
public interface MongoDBMapper {

    // Note: In the development of this type, I made a mistake by misunderstanding the use of how the uses field
    // works in the @Mapper annotation. Future types should make their own mappers and break this up into more
    // bite-sized and manageable chunks. Furthermore, due to the original misunderstanding we should chip away
    // at the bloat in this interface as we continue to develop the database layer.

    // Application Mappings

    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "scriptRepoUrl", ignore = true)
    @Mapping(target = "httpDocumentationUrl", ignore = true)
    @Mapping(target = "httpDocumentationUiUrl", ignore = true)
    @Mapping(target = "httpTunnelEndpointUrl", ignore = true)
    @Mapping(target = "applicationConfiguration", ignore = true)
    Application toApplication(MongoApplication source);

    @InheritInverseConfiguration
    MongoApplication toMongoApplication(Application source);

    // User & Profile Mappings
    @Mapping(target = "id", source = "objectId")
    User toUser(MongoUser source);

    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "hashAlgorithm", ignore = true)
    @InheritInverseConfiguration
    MongoUser toMongoUser(User source);

    @Mapping(target = "id", source = "objectId")
    Profile toProfile(MongoProfile source);

    @Mapping(target = "active", ignore = true)
    @InheritInverseConfiguration
    MongoProfile toMongoProfile(Profile source);

    // Deployment Mappings

    @Mapping(target = "id", source = "objectId")
    Deployment toDeployment(MongoDeployment source);

    @Mapping(target = "createdAt", ignore = true)
    @InheritInverseConfiguration
    MongoDeployment toMongoDeployment(Deployment source);

    // Item Mappings

    @Mapping(target = "id", source = "objectId")
    Item toItem(MongoItem source);

    @InheritInverseConfiguration
    MongoItem toMongoItem(Item source);

    // Inventory Item Mappings

    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "priority", source = "objectId.priority")
    InventoryItem toInventoryItem(MongoInventoryItem source);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "rewardIssuanceUuids", ignore = true)
    @InheritInverseConfiguration(name = "toInventoryItem")
    MongoInventoryItem toMongoInventoryItem(InventoryItem source);

    // Distinct Inventory Mappings

    @Mapping(target = "id", source = "objectId")
    DistinctInventoryItem toDistinctInventoryItem(MongoDistinctInventoryItem source);

    @InheritInverseConfiguration(name = "toDistinctInventoryItem")
    MongoDistinctInventoryItem toMongoDistinctInventoryItem(DistinctInventoryItem source);

    // Missions, Steps, and Rewards

    Step toStep(MongoStep source);

    MongoStep toMongoStep(Step source);

    Reward toReward(MongoReward source);

    MongoReward toMongoReward(Reward source);

    @Mapping(target = "id", source = "objectId")
    RewardIssuance toRewardIssuance(MongoRewardIssuance source);

    @InheritInverseConfiguration
    MongoRewardIssuance toMongoRewardIssuance(RewardIssuance source);

    @Mapping(target = "id", source = "objectId")
    Mission toMission(MongoMission source);

    @InheritInverseConfiguration
    MongoMission toMongoMission(Mission source);

    @Mapping(target = "id", source = "objectId")
    Progress toProgress(MongoProgress source);

    @Mapping(target = "objectId", source = "id")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "missionTags", ignore = true)
    @InheritInverseConfiguration
    MongoProgress toMongoProgress(Progress source);

    @Mapping(target = "id", source = "objectId")
    ProgressMissionInfo toProgressMissionInfo(MongoMission source);

    @InheritInverseConfiguration
    MongoMission toMongoMission(ProgressMissionInfo source);

    // Mission Schedules and Events

    @Mapping(target = "id", source = "objectId")
    Schedule toSchedule(MongoSchedule source);

    @InheritInverseConfiguration
    MongoSchedule toMongoSchedule(Schedule source);

    @Mapping(target = "id", source = "objectId")
    ScheduleEvent toScheduleEvent(MongoScheduleEvent source);

    @InheritInverseConfiguration
    MongoScheduleEvent toMongoScheduleEvent(ScheduleEvent source);

    // FCM Registration

    @Mapping(target = "id", source = "objectId")
    FCMRegistration toFCMRegistration(MongoFCMRegistration source);

    @InheritInverseConfiguration
    MongoFCMRegistration toMongoFCMRegistration(FCMRegistration source);

    // Leaderboards and Scores

    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "scoreUnits", source = "leaderboard.scoreUnits")
    Score toScore(MongoScore source);

    @InheritInverseConfiguration
    MongoScore toMongoScore(Score source);

    @Mapping(target = "id", source = "objectId")
    Leaderboard toLeaderboard(MongoLeaderboard source);

    @InheritInverseConfiguration
    MongoLeaderboard toMongoLeaderboard(Leaderboard source);

    @Mapping(target = "position", ignore = true)
    Rank toRank(MongoScore score);

    // Matches

    @Mapping(source = "objectId", target = "id")
    Match toMatch(MongoMatch source);

    @Mapping(target = "lock", ignore = true)
    @Mapping(target = "expiry", ignore = true)
    @InheritInverseConfiguration
    MongoMatch toMongoMatch(Match source);

    // Large Objects

    LargeObject toLargeObject(MongoLargeObject source);

    MongoLargeObject toMongoLargeObject(LargeObject source);

    LargeObjectReference toLargeObjectReference(MongoLargeObject source);

    @Mapping(target = "path", ignore = true)
    @Mapping(target = "accessPermissions", ignore = true)
    @InheritInverseConfiguration
    MongoLargeObject toMongoLargeObject(LargeObjectReference source);

    // Metadata

    @Mapping(target = "id", source = "objectId")
    MetadataSpec toMetadataSpec(MongoMetadataSpec source);

    @InheritInverseConfiguration
    MongoMetadataSpec toMongoMetadataSpec(MetadataSpec source);

    // Indexing and Metadata

    IndexPlanStep<Document> toIndexPlanStep(MongoIndexPlanStep source);

    MongoIndexPlanStep toMongoIndexPlanStep(IndexPlanStep<Document> source);

    // Crypto Stuff

    @Mapping(target = "id", source = "objectId")
    Vault toVault(MongoVault source);

    @InheritInverseConfiguration
    MongoVault toMongoVault(Vault source);

    @Mapping(target = "id", source = "objectId")
    Wallet toWallet(MongoWallet source);

    @InheritInverseConfiguration
    MongoWallet toMongoWallet(Wallet source);

    @Mapping(target = "id", source = "objectId")
    SmartContract toSmartContract(MongoSmartContract source);

    @InheritInverseConfiguration
    MongoSmartContract toMongoSmartContract(SmartContract source);

    @Mapping(target = "id", source = "objectId")
    ElementsSmartContract toElementsSmartContract(MongoBscSmartContract source);

    @Mapping(target = "id", source = "objectId")
    ElementsSmartContract toElementsSmartContract(MongoNeoSmartContract source);

    @Mapping(target = "objectId", source = "id")
    MongoBscSmartContract toMongoBscSmartContract(ElementsSmartContract source);

    @Mapping(target = "objectId", source = "id")
    MongoNeoSmartContract toMongoNeoSmartContract(ElementsSmartContract source);

    WalletAccount toWalletAccount(MongoWalletAccount source);

    MongoWalletAccount toMongoWalletAccount(WalletAccount source);

    VaultKey toVaultKey(MongoVaultKey source);

    MongoVaultKey toMongoVaultKey(VaultKey source);

    // Save Data Documents

    SaveDataDocument copySaveDataDocument(SaveDataDocument source);

    @Mapping(target = "id", source = "saveDataDocumentId")
    @Mapping(target = "slot", source = "saveDataDocumentId.slot")
    SaveDataDocument toSaveDataDocument(MongoSaveDataDocument source);

    @Mapping(target = "saveDataDocumentId", source = "id")
    @Mapping(target = "saveDataDocumentId.slot", source = "slot")
    @Mapping(target = "digestAlgorithm", ignore = true)
    @InheritInverseConfiguration
    MongoSaveDataDocument toMongoSaveDataDocument(SaveDataDocument source);

    SmartContractAddress toSmartContractAddress(MongoSmartContractAddress mongoSmartContractAddress);

    @Mapping(target = "api", ignore = true)
    @Mapping(target = "network", ignore = true)
    MongoSmartContractAddress toMongoSmartContractAddress(SmartContractAddress smartContractAddress);

    // IAPs
    AppleIapReceipt toAppleIapReceipt(MongoAppleIapReceipt source);

    MongoAppleIapReceipt toMongoAppleIapReceipt(AppleIapReceipt source);

    GooglePlayIapReceipt toGooglePlayIapReceipt(MongoGooglePlayIapReceipt source);

    MongoGooglePlayIapReceipt toMongoGooglePlayIapReceipt(GooglePlayIapReceipt source);

    // Auth & Security

    Session toSession(MongoSession source);

    MongoSession toMongoSession(Session source);

    AuthScheme toAuthScheme(MongoAuthScheme source);

    @InheritInverseConfiguration
    MongoAuthScheme toMongoAuthScheme(AuthScheme source);

    // Custom Conversions

    default List<MongoSmartContractAddress> toAddressesFromMapByNetwork(final Map<BlockchainNetwork, SmartContractAddress> map) {
        return map.entrySet()
                .stream()
                .map(entry -> {
                    final var mongoSmartContractAddress = toMongoSmartContractAddress(entry.getValue());
                    mongoSmartContractAddress.setNetwork(entry.getKey());
                    mongoSmartContractAddress.setApi(entry.getKey().api());
                    return mongoSmartContractAddress;
                })
                .collect(toList());
    }

    default Map<BlockchainNetwork, SmartContractAddress> toAddressesByNetwork(final List<MongoSmartContractAddress> list) {
        return list
                .stream()
                .filter(Objects::nonNull)
                .filter(addr -> addr.getNetwork() != null)
                .collect(toMap(MongoSmartContractAddress::getNetwork, this::toSmartContractAddress));
    }

    default IndexMetadata<Document> toIndexMetadata(final MongoIndexMetadata source) {
        return source;
    }

    default MongoIndexMetadata toMongoIndexMetadata(final IndexMetadata<Document> source) {
        final var mongoIndexMetadata = new MongoIndexMetadata();
        mongoIndexMetadata.setKeys(source.getIdentifier());
        return mongoIndexMetadata;
    }

}
