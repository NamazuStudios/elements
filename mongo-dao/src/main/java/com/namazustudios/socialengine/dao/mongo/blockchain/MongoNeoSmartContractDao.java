package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoToken;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import javax.inject.Inject;

public class MongoNeoSmartContractDao implements NeoSmartContractDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<NeoSmartContract> getNeoSmartContract(int offset, int count, String search) {
        return null;
    }

    @Override
    public NeoSmartContract getNeoSmartContract(String contractIdOrName) {
        return null;
    }

    @Override
    public NeoSmartContract patchNeoSmartContract(UpdateNeoSmartContractRequest updateNeoSmartContractRequest) {
        return null;
    }

    @Override
    public void deleteNeoSmartContract(String contractId) {

    }

    private NeoToken transform(MongoNeoToken token)
    {
        return getBeanMapper().map(token, NeoToken.class);
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
