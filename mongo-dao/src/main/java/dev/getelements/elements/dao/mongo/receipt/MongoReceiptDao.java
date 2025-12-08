package dev.getelements.elements.dao.mongo.receipt;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.model.receipt.MongoReceipt;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoReceiptDao implements ReceiptDao {
    
    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    @Override
    public Pagination<Receipt> getReceipts(User user, int offset, int count, String search) {
        final Query<MongoReceipt> query = getDatastore().find(MongoReceipt.class);

        query.filter(Filters.eq("user", getDozerMapper().map(user, MongoUser.class)));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoReceipt -> getDozerMapper().map(mongoReceipt, Receipt.class), new FindOptions());
    }

    @Override
    public Receipt getReceipt(String schema, String originalTransactionId) {
        if (isEmpty(nullToEmpty(originalTransactionId).trim())) {
            throw new NotFoundException("Unable to find apple iap receipt with an id of " + originalTransactionId);
        }

        final Query<MongoReceipt> receiptQuery = getDatastore().find(MongoReceipt.class);

        receiptQuery.filter(Filters.eq("_id", originalTransactionId));

        final MongoReceipt mongoReceipt = receiptQuery.first();

        if(null == mongoReceipt) {
            throw new NotFoundException("Unable to find apple iap receipt with an id of " + originalTransactionId);
        }

        return getDozerMapper().map(mongoReceipt, Receipt.class);
    }

    @Override
    public Receipt getOrCreateReceipt(Receipt receipt) {
        getValidationHelper().validateModel(receipt, ValidationGroups.Insert.class);

        try {
            return getReceipt(receipt.getSchema(), receipt.getOriginalTransactionId());
        }
        catch (NotFoundException e) {
            // do nothing
        }

        final MongoReceipt mongoReceipt =
                getDozerMapper().map(receipt, MongoReceipt.class);

        try {
            getDatastore().insert(mongoReceipt);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        final Query<MongoReceipt> receiptQuery = getDatastore().find(MongoReceipt.class);

        receiptQuery.filter(Filters.eq("_id", receipt.getOriginalTransactionId()));

        return getDozerMapper().map(receiptQuery.first(), Receipt.class);
    }

    @Override
    public void deleteReceipt(String id) {

        final var receiptQuery = getDatastore().find(MongoReceipt.class);

        if(!ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find receipt with an id of " + id);
        }

        final var objectId = new ObjectId(id);

        receiptQuery.filter(Filters.eq("_id", objectId));

        final DeleteResult deleteResult = getDatastore().delete(Objects.requireNonNull(receiptQuery.first()));

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Receipt not found: " + id);
        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
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
