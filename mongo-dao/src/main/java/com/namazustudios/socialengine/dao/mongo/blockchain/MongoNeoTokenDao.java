package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoToken;
import com.namazustudios.socialengine.exception.blockchain.NeoTokenNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.neo.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.model.blockchain.neo.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
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

public class MongoNeoTokenDao implements NeoTokenDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<NeoToken> getTokens(final int offset,
                                          final int count,
                                          final List<String> tags,
                                          final BlockchainConstants.MintStatus mintStatus,
                                          final String search) {

        final var trimmedSearch = nullToEmpty(search).trim();
        final var mongoQuery = getDatastore().find(MongoNeoToken.class);

        tags.remove("");

        if (!tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        if (!trimmedSearch.isEmpty()) {
            mongoQuery.filter(Filters.regex("name").pattern(Pattern.compile(trimmedSearch)));
        }

        if (mintStatus != null) {
            mongoQuery.filter(Filters.eq("mintStatus", mintStatus));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public NeoToken getToken(String tokenIdOrName) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(tokenIdOrName);

        var mongoToken = getDatastore()
            .find(MongoNeoToken.class)
            .filter(Filters.eq("_id", objectId))
            .first();

        if(mongoToken == null) {
            throw new NeoTokenNotFoundException("Unable to find token with an id of " + tokenIdOrName);
        }

        return transform(mongoToken);
    }

    @Override
    public NeoToken updateToken(String tokenId, UpdateNeoTokenRequest updateNeoTokenRequest) {
        getValidationHelper().validateModel(updateNeoTokenRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoNeoToken.class);
        final var token = updateNeoTokenRequest.getToken();
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
            set("listed", updateNeoTokenRequest.isListed()),
            set("contractId", updateNeoTokenRequest.getContractId())
        );

        final MongoNeoToken mongoNeoToken = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoToken == null) {
            throw new NeoTokenNotFoundException("NeoToken not found or was already minted: " + tokenId);
        }

        getObjectIndex().index(mongoNeoToken);
        return transform(mongoNeoToken);
    }

    @Override
    public NeoToken setMintStatusForToken(final String tokenId, final BlockchainConstants.MintStatus status) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoNeoToken.class);

        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder().with(
            set("mintStatus", status)
        );

        final MongoNeoToken mongoNeoToken = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoToken == null) {
            throw new NeoTokenNotFoundException("NeoToken not found: " + tokenId);
        }

        getObjectIndex().index(mongoNeoToken);
        return transform(mongoNeoToken);
    }

    @Override
    public NeoToken createToken(CreateNeoTokenRequest tokenRequest) {
        getValidationHelper().validateModel(tokenRequest, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(tokenRequest.getToken(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoNeoToken.class);
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

        getObjectIndex().index(mongoToken);
        return transform(mongoToken);
    }

    @Override
    public NeoToken cloneNeoToken(NeoToken neoToken) {
        getValidationHelper().validateModel(neoToken.getToken(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoNeoToken.class);
        final var totalMintedQuantity = neoToken.getTotalMintedQuantity() + 1;
        final var token = neoToken.getToken();
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
                set("listed", neoToken.isListed()),
                set("mintStatus", BlockchainConstants.MintStatus.NOT_MINTED),
                set("contractId", neoToken.getContractId()),
                set("seriesId", neoToken.getSeriesId()),
                set("totalMintedQuantity", totalMintedQuantity)
        );

        final var mongoToken = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        setTotalMintedQuantity(neoToken.getId(), totalMintedQuantity);

        getObjectIndex().index(mongoToken);
        return transform(mongoToken);
    }

    @Override
    public void deleteToken(String tokenId) {
        final var objectId = getMongoDBUtils().parseOrThrow(tokenId, NeoTokenNotFoundException::new);

        final var result = getDatastore()
                .find(MongoNeoToken.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new NeoTokenNotFoundException("NeoToken not deleted: " + tokenId);
        }
    }

    private NeoToken transform(MongoNeoToken token)
    {
        return getBeanMapper().map(token, NeoToken.class);
    }

    public void setTotalMintedQuantity(String tokenId, long quantity) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoNeoToken.class);

        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder().with(
                set("totalMintedQuantity", quantity)
        );

        final MongoNeoToken mongoNeoToken = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoToken == null) {
            throw new NeoTokenNotFoundException("NeoToken not found: " + tokenId);
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

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

}
