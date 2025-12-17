package dev.getelements.elements.dao.mongo.receipt;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.model.receipt.MongoReceipt;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.filters.Filters.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoReceiptDao implements ReceiptDao {
    
    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    private Consumer<Event> eventPublisher;

    @Override
    public Pagination<Receipt> getReceipts(User user, int offset, int count, String search) {

        final var trimmedQueryString = nullToEmpty(search).trim();

        if (trimmedQueryString.isEmpty()) {
            throw new InvalidDataException("search must be specified.");
        }

        final var query = getDatastore().find(MongoReceipt.class);

        if(user != null) {
            query.filter(eq("user", getDozerMapper().map(user, MongoUser.class)));
        }

        query.filter(
                or(
                        regex("originalTransactionId", Pattern.compile(search)),
                        regex("schema", Pattern.compile(search))
                )
        );

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoReceipt -> getDozerMapper().map(mongoReceipt, Receipt.class), new FindOptions());
    }

    @Override
    public Pagination<Receipt> getReceipts(User user, int offset, int count) {

        final var query = getDatastore().find(MongoReceipt.class);

        if(user != null) {
            query.filter(eq("user", getDozerMapper().map(user, MongoUser.class)));
        }

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoReceipt -> getDozerMapper().map(mongoReceipt, Receipt.class), new FindOptions());
    }

    @Override
    public Receipt getReceipt(String id) {
        
        if (isEmpty(nullToEmpty(id).trim()) || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find receipt with an id of " + id);
        }

        final Query<MongoReceipt> receiptQuery = getDatastore().find(MongoReceipt.class);

        receiptQuery.filter(
                        eq("id", new ObjectId(id))
        );

        final MongoReceipt mongoReceipt = receiptQuery.first();

        if(null == mongoReceipt) {
            throw new NotFoundException("Unable to find receipt with an id of " + id);
        }

        return getDozerMapper().map(mongoReceipt, Receipt.class);
    }

    @Override
    public Receipt getReceipt(String schema, String originalTransactionId) {
        if (isEmpty(nullToEmpty(originalTransactionId).trim())) {
            throw new NotFoundException("Unable to find receipt with an id of " + originalTransactionId);
        }

        final Query<MongoReceipt> receiptQuery = getDatastore().find(MongoReceipt.class);

        receiptQuery.filter(
                and(
                        eq("originalTransactionId", originalTransactionId),
                        eq("schema", schema)
                )

        );

        final MongoReceipt mongoReceipt = receiptQuery.first();

        if(null == mongoReceipt) {
            throw new NotFoundException("Unable to find receipt with an id of " + originalTransactionId);
        }

        return getDozerMapper().map(mongoReceipt, Receipt.class);
    }

    @Override
    public Receipt createReceipt(Receipt receipt) {

        getValidationHelper().validateModel(receipt, ValidationGroups.Insert.class);

        try {
            final var checkById = receipt.getId() != null && ObjectId.isValid(receipt.getId());
            return checkById ? getReceipt(receipt.getId()) : getReceipt(receipt.getSchema(), receipt.getOriginalTransactionId());
        }
        catch (NotFoundException e) {
            // do nothing
        }

        final var mongoReceipt = getDozerMapper().map(receipt, MongoReceipt.class);

        try {
            final var saveResult = getDatastore().save(mongoReceipt);
            final var response = getDozerMapper().map(saveResult, Receipt.class);

            getEventPublisher().accept(Event.builder()
                    .argument(response)
                    .named(RECEIPT_CREATED)
                    .build());

            return response;
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
    }

    @Override
    public void deleteReceipt(String id) {

        final var receiptQuery = getDatastore().find(MongoReceipt.class);

        if(!ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find receipt with an id of " + id);
        }

        final var objectId = new ObjectId(id);

        receiptQuery.filter(eq("_id", objectId));

        try {
            final var receipt = Objects.requireNonNull(receiptQuery.first());

            final DeleteResult deleteResult = getDatastore().delete(receipt);

            if (deleteResult.getDeletedCount() == 0) {
                throw new NotFoundException("Receipt not found: " + id);
            }
        } catch (NullPointerException e) {
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

    public Consumer<Event> getEventPublisher() {
        return eventPublisher;
    }

    @Inject
    public void setEventPublisher(Consumer<Event> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

}
