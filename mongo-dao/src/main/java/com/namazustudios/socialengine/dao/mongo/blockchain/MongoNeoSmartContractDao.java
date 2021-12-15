package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoSmartContract;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoToken;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;

import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoNeoSmartContractDao implements NeoSmartContractDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<NeoSmartContract> getNeoSmartContracts(int offset, int count, String search) {
        final String trimmedSearch = nullToEmpty(search).trim();
        final Query<MongoNeoSmartContract> mongoQuery = getDatastore().find(MongoNeoSmartContract.class);

        if (!trimmedSearch.isEmpty()) {
            mongoQuery.filter(Filters.regex("displayName").pattern(Pattern.compile(trimmedSearch)));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public NeoSmartContract getNeoSmartContract(String contractIdOrName) {
        final var objectId = getMongoDBUtils().parseOrReturnNull(contractIdOrName);

        var mongoContract = getDatastore().find(MongoNeoSmartContract.class)
                .filter(Filters.or(
                                Filters.eq("_id", objectId),
                                Filters.eq("name", contractIdOrName)
                        )
                ).first();

        if(null == mongoContract) {
            throw new NotFoundException("Unable to find contract with an id or name of " + contractIdOrName);
        }

        return transform(mongoContract);
    }

    @Override
    public NeoSmartContract patchNeoSmartContract(PatchNeoSmartContractRequest patchNeoSmartContractRequest) {
        getValidationHelper().validateModel(patchNeoSmartContractRequest, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoNeoSmartContract.class);
        final var scriptHash = patchNeoSmartContractRequest.getScriptHash();

        query.filter(eq("scriptHash", scriptHash));

        final var builder = new UpdateBuilder().with(
                set("displayName", patchNeoSmartContractRequest.getDisplayName()),
                set("scriptHash", scriptHash)
        );

        var metadata = patchNeoSmartContractRequest.getMetadata();
        if (metadata != null) {
            builder.with(set("metadata", metadata));
        }

        final var mongoContract = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoContract);
        return transform(mongoContract);
    }

    @Override
    public void deleteNeoSmartContract(String contractId) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(contractId);
        final var query = getDatastore().find(MongoNeoSmartContract.class);

        query.filter(eq("_id", objectId));
        query.delete();
    }

    private NeoSmartContract transform(MongoNeoSmartContract token) {
        return getBeanMapper().map(token, NeoSmartContract.class);
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
