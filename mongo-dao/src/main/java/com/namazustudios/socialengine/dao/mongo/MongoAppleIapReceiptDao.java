package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.AppleIapReceiptDao;
import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoAppleIapReceipt;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoMission;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoReward;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoStep;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Streams.concat;
import static java.util.stream.Collectors.toList;
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
        final Query<MongoAppleIapReceipt> query = getDatastore().createQuery(MongoAppleIapReceipt.class);

        query.field("user").equal(getDozerMapper().map(user, MongoUser.class));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoAppleIapReceipt -> getDozerMapper().map(mongoAppleIapReceipt, AppleIapReceipt.class));
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionIdentifier) {
        if (isEmpty(nullToEmpty(originalTransactionIdentifier).trim())) {
            throw new NotFoundException("Unable to find apple iap receipt with an id of " + originalTransactionIdentifier);
        }

        final Query<MongoAppleIapReceipt> receiptQuery = getDatastore().createQuery(MongoAppleIapReceipt.class);

        receiptQuery.criteria("_id").equal(originalTransactionIdentifier);

        final MongoAppleIapReceipt mongoAppleIapReceipt = receiptQuery.get();

        if(null == mongoAppleIapReceipt) {
            throw new NotFoundException("Unable to find apple iap receipt with an id of " + originalTransactionIdentifier);
        }

        return getDozerMapper().map(mongoAppleIapReceipt, AppleIapReceipt.class);
    }

    @Override
    public AppleIapReceipt createAppleIapReceipt(AppleIapReceipt appleIapReceipt) {
        getValidationHelper().validateModel(appleIapReceipt, Insert.class);

        final MongoAppleIapReceipt mongoAppleIapReceipt =
                getDozerMapper().map(appleIapReceipt, MongoAppleIapReceipt.class);

        try {
            getDatastore().insert(mongoAppleIapReceipt);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        getObjectIndex().index(mongoAppleIapReceipt);

        return getDozerMapper().map(getDatastore().get(mongoAppleIapReceipt), AppleIapReceipt.class);
    }

    @Override
    public void deleteAppleIapReceipt(String originalTransactionIdentifier) {
        final WriteResult writeResult = getDatastore().delete(MongoAppleIapReceipt.class, originalTransactionIdentifier);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Apple IAP Receipt not found: " + originalTransactionIdentifier);
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
