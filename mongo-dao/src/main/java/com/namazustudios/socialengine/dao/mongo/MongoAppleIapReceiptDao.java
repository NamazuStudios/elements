package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.mongodb.client.result.DeleteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.AppleIapReceiptDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoAppleIapReceipt;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.DeleteOptions;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Streams.concat;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoAppleIapReceiptDao implements AppleIapReceiptDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAppleIapReceiptDao.class);

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    @Override
    public Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count) {
        final Query<MongoAppleIapReceipt> query = getDatastore().find(MongoAppleIapReceipt.class);

        query.field("user").equal(getDozerMapper().map(user, MongoUser.class));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoAppleIapReceipt -> getDozerMapper().map(mongoAppleIapReceipt, AppleIapReceipt.class));
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionId) {
        if (isEmpty(nullToEmpty(originalTransactionId).trim())) {
            throw new NotFoundException("Unable to find apple iap receipt with an id of " + originalTransactionId);
        }

        final Query<MongoAppleIapReceipt> receiptQuery = getDatastore().find(MongoAppleIapReceipt.class);

        receiptQuery.filter(Filters.eq("_id", originalTransactionId));

        final MongoAppleIapReceipt mongoAppleIapReceipt = receiptQuery.first();

        if(null == mongoAppleIapReceipt) {
            throw new NotFoundException("Unable to find apple iap receipt with an id of " + originalTransactionId);
        }

        return getDozerMapper().map(mongoAppleIapReceipt, AppleIapReceipt.class);
    }

    @Override
    public AppleIapReceipt getOrCreateAppleIapReceipt(AppleIapReceipt appleIapReceipt) {
        getValidationHelper().validateModel(appleIapReceipt, Insert.class);

        try {
            AppleIapReceipt resultAppleIapReceipt = getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());
            return resultAppleIapReceipt;
        }
        catch (NotFoundException e) {
            // do nothing
        }

        final MongoAppleIapReceipt mongoAppleIapReceipt =
                getDozerMapper().map(appleIapReceipt, MongoAppleIapReceipt.class);

        try {
            getDatastore().insert(mongoAppleIapReceipt);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        getObjectIndex().index(mongoAppleIapReceipt);

        final Query<MongoAppleIapReceipt> receiptQuery = getDatastore().find(MongoAppleIapReceipt.class);

        receiptQuery.filter(Filters.eq("_id", appleIapReceipt.getOriginalTransactionId()));

        return getDozerMapper().map(receiptQuery.first(), AppleIapReceipt.class);
    }

    @Override
    public void deleteAppleIapReceipt(final String originalTransactionId) {

        final Query<MongoAppleIapReceipt> receiptQuery = getDatastore().find(MongoAppleIapReceipt.class);

        receiptQuery.filter(Filters.eq("_id", originalTransactionId));
        
        final DeleteResult deleteResult = getDatastore().delete(receiptQuery.first());

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Apple IAP Receipt not found: " + originalTransactionId);
        }
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }
}
