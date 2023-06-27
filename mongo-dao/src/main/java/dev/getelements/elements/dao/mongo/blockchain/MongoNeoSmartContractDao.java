package dev.getelements.elements.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import dev.getelements.elements.dao.NeoSmartContractDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoNeoSmartContract;
import dev.getelements.elements.exception.blockchain.NeoSmartContractNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.blockchain.*;
import dev.getelements.elements.util.ValidationHelper;
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
import static dev.morphia.query.experimental.updates.UpdateOperators.unset;

public class MongoNeoSmartContractDao implements NeoSmartContractDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<ElementsSmartContract> getNeoSmartContracts(int offset, int count, String search) {
        final String trimmedSearch = nullToEmpty(search).trim();
        final Query<MongoNeoSmartContract> mongoQuery = getDatastore().find(MongoNeoSmartContract.class);

        if (!trimmedSearch.isEmpty()) {
            mongoQuery.filter(Filters.regex("displayName").pattern(Pattern.compile(trimmedSearch)));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public ElementsSmartContract getNeoSmartContract(String contractIdOrName) {
        final var objectId = getMongoDBUtils().parseOrReturnNull(contractIdOrName);

        var mongoContract = getDatastore().find(MongoNeoSmartContract.class)
                .filter(Filters.or(
                                Filters.eq("_id", objectId),
                                Filters.eq("displayName", contractIdOrName)
                        )
                ).first();

        if(mongoContract == null) {
            throw new NeoSmartContractNotFoundException("Unable to find contract with an id or name of " + contractIdOrName);
        }

        return transform(mongoContract);
    }

    @Override
    public ElementsSmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest) {
        getValidationHelper().validateModel(patchSmartContractRequest, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoNeoSmartContract.class);
        final var scriptHash = patchSmartContractRequest.getScriptHash();

        query.filter(eq("scriptHash", scriptHash));

        final var builder = new UpdateBuilder().with(
                set("displayName", patchSmartContractRequest.getDisplayName()),
                set("scriptHash", scriptHash),
                set("blockchain", patchSmartContractRequest.getBlockchain())
        );

        final var walletId = patchSmartContractRequest.getWalletId();
        if(walletId != null) {

            builder.with(set("walletId", walletId));

            final var accountAddress = patchSmartContractRequest.getAccountAddress();

            if(accountAddress != null) {
                builder.with(set("accountAddress", accountAddress));
            } else {
                builder.with(unset("accountAddress"));
            }

        } else {
            builder.with(unset("walletId"));
            builder.with(unset("accountAddress"));
        }

        final var metadata = patchSmartContractRequest.getMetadata();
        if (metadata != null) {
            builder.with(set("metadata", metadata));
        } else {
            builder.with(unset("metadata"));
        }

        final var mongoContract = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        if(mongoContract == null) {
            throw new NeoSmartContractNotFoundException("Unable to find contract with a matching script hash " + scriptHash);
        }

        getObjectIndex().index(mongoContract);
        return transform(mongoContract);
    }

    @Override
    public void deleteNeoSmartContract(String contractId) {
        final var objectId = getMongoDBUtils().parseOrThrow(contractId, NeoSmartContractNotFoundException::new);

        final var result = getDatastore()
                .find(MongoNeoSmartContract.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new NeoSmartContractNotFoundException("NeoSmartContract not deleted: " + contractId);
        }
    }

    private ElementsSmartContract transform(MongoNeoSmartContract token) {
        return getBeanMapper().map(token, ElementsSmartContract.class);
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
