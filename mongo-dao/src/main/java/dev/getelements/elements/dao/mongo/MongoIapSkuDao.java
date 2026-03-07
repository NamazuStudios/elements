package dev.getelements.elements.dao.mongo;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.model.iap.MongoIapSku;
import dev.getelements.elements.sdk.dao.IapSkuDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;

import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;

public class MongoIapSkuDao implements IapSkuDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    @Override
    public IapSku getIapSku(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find IAP SKU with id: " + id);
        }

        final var mongoIapSku = getDatastore().find(MongoIapSku.class)
                .filter(eq("_id", new ObjectId(id)))
                .first();

        if (mongoIapSku == null) {
            throw new NotFoundException("Unable to find IAP SKU with id: " + id);
        }

        return getDozerMapperRegistry().map(mongoIapSku, IapSku.class);
    }

    @Override
    public IapSku getIapSku(final String schema, final String productId) {

        final var mongoIapSku = getDatastore().find(MongoIapSku.class)
                .filter(and(eq("schema", schema), eq("productId", productId)))
                .first();

        if (mongoIapSku == null) {
            throw new NotFoundException("Unable to find IAP SKU for schema=" + schema + " productId=" + productId);
        }

        return getDozerMapperRegistry().map(mongoIapSku, IapSku.class);
    }

    @Override
    public Pagination<IapSku> getIapSkus(final int offset, final int count) {

        final var query = getDatastore().find(MongoIapSku.class);

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoIapSku -> getDozerMapperRegistry().map(mongoIapSku, IapSku.class),
                new FindOptions());
    }

    @Override
    public Pagination<IapSku> getIapSkus(final String schema, final int offset, final int count) {

        final var query = getDatastore().find(MongoIapSku.class);
        query.filter(eq("schema", schema));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoIapSku -> getDozerMapperRegistry().map(mongoIapSku, IapSku.class),
                new FindOptions());
    }

    @Override
    public IapSku createIapSku(final IapSku iapSku) {

        getValidationHelper().validateModel(iapSku, ValidationGroups.Insert.class);

        final var mongoIapSku = getDozerMapperRegistry().map(iapSku, MongoIapSku.class);

        try {
            getDatastore().insert(mongoIapSku);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) throw new DuplicateException(e);
            throw e;
        }

        return getDozerMapperRegistry().map(mongoIapSku, IapSku.class);
    }

    @Override
    public IapSku updateIapSku(final IapSku iapSku) {

        getValidationHelper().validateModel(iapSku, ValidationGroups.Update.class);

        final var mongoIapSku = getDozerMapperRegistry().map(iapSku, MongoIapSku.class);
        final var saved = getDatastore().save(mongoIapSku);

        return getDozerMapperRegistry().map(saved, IapSku.class);
    }

    @Override
    public void deleteIapSku(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find IAP SKU with id: " + id);
        }

        final DeleteResult deleteResult = getDatastore().find(MongoIapSku.class)
                .filter(eq("_id", new ObjectId(id)))
                .delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("IAP SKU not found: " + id);
        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MapperRegistry getDozerMapperRegistry() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapperRegistry(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
