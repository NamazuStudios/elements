package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.GooglePlayIapReceiptDao;
import dev.getelements.elements.dao.mongo.model.MongoGooglePlayIapReceipt;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoGooglePlayIapReceiptDao implements GooglePlayIapReceiptDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoGooglePlayIapReceiptDao.class);

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    @Override
    public Pagination<GooglePlayIapReceipt> getGooglePlayIapReceipts(User user, int offset, int count) {
        final Query<MongoGooglePlayIapReceipt> query = getDatastore().find(MongoGooglePlayIapReceipt.class);

        query.filter(Filters.eq("user", getDozerMapper().map(user, MongoUser.class)));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoGooglePlayIapReceipt ->
                        getDozerMapper().map(mongoGooglePlayIapReceipt, GooglePlayIapReceipt.class), new FindOptions()
        );
    }

    @Override
    public GooglePlayIapReceipt getGooglePlayIapReceipt(String orderId) {
        if (isEmpty(nullToEmpty(orderId).trim())) {
            throw new NotFoundException("Unable to find google play iap receipt with an id of " + orderId);
        }

        final Query<MongoGooglePlayIapReceipt> receiptQuery =
                getDatastore().find(MongoGooglePlayIapReceipt.class);

        receiptQuery.filter(Filters.eq("_id", orderId));

        final MongoGooglePlayIapReceipt mongoGooglePlayIapReceipt = receiptQuery.first();

        if(null == mongoGooglePlayIapReceipt) {
            throw new NotFoundException("Unable to find google play iap receipt with an id of " + orderId);
        }

        return getDozerMapper().map(mongoGooglePlayIapReceipt, GooglePlayIapReceipt.class);
    }

    @Override
    public GooglePlayIapReceipt getOrCreateGooglePlayIapReceipt(GooglePlayIapReceipt googlePlayIapReceipt) {
        getValidationHelper().validateModel(googlePlayIapReceipt, Insert.class);

        try {
            GooglePlayIapReceipt resultGooglePlayIapReceipt =
                    getGooglePlayIapReceipt(googlePlayIapReceipt.getOrderId());
            return resultGooglePlayIapReceipt;
        }
        catch (NotFoundException e) {
            // do nothing
        }

        final MongoGooglePlayIapReceipt mongoGooglePlayIapReceipt =
                getDozerMapper().map(googlePlayIapReceipt, MongoGooglePlayIapReceipt.class);

        try {
            getDatastore().insert(mongoGooglePlayIapReceipt);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        final Query<MongoGooglePlayIapReceipt> query = getDatastore().find(MongoGooglePlayIapReceipt.class);
        query.filter(Filters.eq("_id", mongoGooglePlayIapReceipt.getOrderId()));
        return getDozerMapper().map(query.first(), GooglePlayIapReceipt.class);
    }

    @Override
    public void deleteGooglePlayIapReceipt(String orderId) {
        final DeleteResult deleteResult = getDatastore().find(MongoGooglePlayIapReceipt.class)
                .filter(Filters.eq("_id", orderId)).delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Google Play IAP Receipt not found: " + orderId);
        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }
}
