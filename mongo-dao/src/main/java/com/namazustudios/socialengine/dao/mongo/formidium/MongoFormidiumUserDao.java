package com.namazustudios.socialengine.dao.mongo.formidium;

import com.namazustudios.socialengine.dao.FormidiumUserDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import javax.inject.Inject;

public class MongoFormidiumUserDao implements FormidiumUserDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoDBUtils mongoDBUtils;

    private ValidationHelper validationHelper;

    @Override
    public FormidiumInvestor createInvestor(final String investorId, final String userId) {
        final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        

        return null;
    }

    @Override
    public Pagination<FormidiumInvestor> getFormidiumInvestors(final String userId, final int offset, final int count) {
        final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        return null;
    }

    @Override
    public FormidiumInvestor getFormidiumInvestor(final String formidiumInvestorId, final String userId) {
        final var mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        return null;
    }

    @Override
    public void deleteFormidiumInvestor(final String formidiumInvestorId) {

    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
