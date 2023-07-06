package dev.getelements.elements.dao.mongo.blockchain;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.dao.BscTokenDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoBscToken;
import dev.getelements.elements.exception.blockchain.BscTokenNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.blockchain.bsc.BscToken;
import dev.getelements.elements.model.blockchain.bsc.CreateBscTokenRequest;
import dev.getelements.elements.model.blockchain.bsc.UpdateBscTokenRequest;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoBscTokenDao implements BscTokenDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<BscToken> getTokens(final int offset,
                                          final int count,
                                          final List<String> tags,
                                          final List<BlockchainConstants.MintStatus> mintStatus,
                                          final String search) {

        final var trimmedSearch = nullToEmpty(search).trim();
        final var mongoQuery = getDatastore().find(MongoBscToken.class);

        tags.remove("");

        if (!tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        if (!trimmedSearch.isEmpty()) {
            mongoQuery.filter(Filters.regex("name").pattern(Pattern.compile(trimmedSearch)));
        }

        if (mintStatus != null) {
            mongoQuery.filter(Filters.in("mintStatus", mintStatus));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public BscToken getToken(String tokenIdOrName) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(tokenIdOrName);

        var mongoToken = getDatastore()
            .find(MongoBscToken.class)
            .filter(Filters.eq("_id", objectId))
            .first();

        if(mongoToken == null) {
            throw new BscTokenNotFoundException("Unable to find token with an id of " + tokenIdOrName);
        }

        return transform(mongoToken);
    }

    @Override
    public BscToken updateToken(String tokenId, UpdateBscTokenRequest updateBscTokenRequest) {
        getValidationHelper().validateModel(updateBscTokenRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoBscToken.class);
        final var token = updateBscTokenRequest.getToken();
        final var name = token.getName().trim();
        final var tags = token.getTags();
        tags.remove("");

        query.filter(and(
            eq("_id", objectId),
            eq("mintStatus", BlockchainConstants.MintStatus.MINTED).not(),
            lt("totalMintedQuantity", 1)
        ));

        final var builder = new UpdateBuilder().with(
            set("name", name),
            set("tags", tags),
            set("token", token),
            set("listed", updateBscTokenRequest.isListed()),
            set("contractId", updateBscTokenRequest.getContractId())
        );

        final MongoBscToken mongoBscToken = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoBscToken == null) {
            throw new BscTokenNotFoundException("BscToken not found or was already minted: " + tokenId);
        }

        return transform(mongoBscToken);
    }

    @Override
    public BscToken setMintStatusForToken(final String tokenId, final BlockchainConstants.MintStatus status) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoBscToken.class);

        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder().with(
            set("mintStatus", status)
        );

        final MongoBscToken mongoBscToken = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoBscToken == null) {
            throw new BscTokenNotFoundException("BscToken not found: " + tokenId);
        }

        return transform(mongoBscToken);
    }

    @Override
    public BscToken createToken(CreateBscTokenRequest tokenRequest) {
        getValidationHelper().validateModel(tokenRequest, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(tokenRequest.getToken(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoBscToken.class);
        final var token = tokenRequest.getToken();
        final var name = token.getName().trim();
        final var tags = token.getTags();
        tags.remove("");

        query.filter(exists("name").not());

        final var builder = new UpdateBuilder().with(
            set("name", name),
            set("tokenUUID", UUID.randomUUID().toString()),
            set("tags", tags),
            set("token", token),
            set("listed", tokenRequest.isListed()),
            set("mintStatus", BlockchainConstants.MintStatus.NOT_MINTED),
            set("contractId", tokenRequest.getContractId()),
            set("seriesId", UUID.randomUUID().toString()),
            set("totalMintedQuantity", 0)
        );

        final var mongoToken = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return transform(mongoToken);
    }

    @Override
    public BscToken cloneBscToken(BscToken bscToken) {
        getValidationHelper().validateModel(bscToken.getToken(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoBscToken.class);
        final var totalMintedQuantity = bscToken.getTotalMintedQuantity() + 1;
        final var token = bscToken.getToken();
        final var name = token.getName() + "_" + totalMintedQuantity;
        token.setName(name);
        final var tags = token.getTags();
        tags.remove("");

        query.filter(exists("name").not());

        final var builder = new UpdateBuilder().with(
                set("name", name),
                set("tokenUUID", UUID.randomUUID().toString()),
                set("tags", tags),
                set("token", token),
                set("listed", bscToken.isListed()),
                set("mintStatus", BlockchainConstants.MintStatus.NOT_MINTED),
                set("contractId", bscToken.getContractId()),
                set("seriesId", bscToken.getSeriesId()),
                set("totalMintedQuantity", totalMintedQuantity)
        );

        final var mongoToken = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        setTotalMintedQuantity(bscToken.getId(), totalMintedQuantity);

        return transform(mongoToken);
    }

    @Override
    public void deleteToken(String tokenId) {
        final var objectId = getMongoDBUtils().parseOrThrow(tokenId, BscTokenNotFoundException::new);

        final var result = getDatastore()
                .find(MongoBscToken.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new BscTokenNotFoundException("BscToken not deleted: " + tokenId);
        }
    }

    private BscToken transform(MongoBscToken token)
    {
        return getBeanMapper().map(token, BscToken.class);
    }

    public void setTotalMintedQuantity(String tokenId, long quantity) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoBscToken.class);

        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder().with(
                set("totalMintedQuantity", quantity)
        );

        final MongoBscToken mongoBscToken = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoBscToken == null) {
            throw new BscTokenNotFoundException("BscToken not found: " + tokenId);
        }
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
