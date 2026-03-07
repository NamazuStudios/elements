package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductSku;
import dev.getelements.elements.sdk.dao.ProductSkuDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;

import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;

public class MongoProductSkuDao implements ProductSkuDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    @Override
    public ProductSku getProductSku(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product SKU with id: " + id);
        }

        final var mongoProductSku = getDatastore().find(MongoProductSku.class)
                .filter(eq("_id", new ObjectId(id)))
                .first();

        if (mongoProductSku == null) {
            throw new NotFoundException("Unable to find Product SKU with id: " + id);
        }

        return getDozerMapperRegistry().map(mongoProductSku, ProductSku.class);
    }

    @Override
    public ProductSku getProductSku(final String schema, final String productId) {

        final var mongoProductSku = getDatastore().find(MongoProductSku.class)
                .filter(and(eq("schema", schema), eq("productId", productId)))
                .first();

        if (mongoProductSku == null) {
            throw new NotFoundException("Unable to find Product SKU for schema=" + schema + " productId=" + productId);
        }

        return getDozerMapperRegistry().map(mongoProductSku, ProductSku.class);
    }

    @Override
    public Pagination<ProductSku> getProductSkus(final int offset, final int count) {

        final var query = getDatastore().find(MongoProductSku.class);

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoProductSku -> getDozerMapperRegistry().map(mongoProductSku, ProductSku.class),
                new FindOptions());
    }

    @Override
    public Pagination<ProductSku> getProductSkus(final String schema, final int offset, final int count) {

        final var query = getDatastore().find(MongoProductSku.class);
        query.filter(eq("schema", schema));

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoProductSku -> getDozerMapperRegistry().map(mongoProductSku, ProductSku.class),
                new FindOptions());
    }

    @Override
    public ProductSku createProductSku(final ProductSku productSku) {

        getValidationHelper().validateModel(productSku, ValidationGroups.Insert.class);

        final var mongoProductSku = getDozerMapperRegistry().map(productSku, MongoProductSku.class);

        try {
            getDatastore().insert(mongoProductSku);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) throw new DuplicateException(e);
            throw e;
        }

        return getDozerMapperRegistry().map(mongoProductSku, ProductSku.class);
    }

    @Override
    public ProductSku updateProductSku(final ProductSku productSku) {

        getValidationHelper().validateModel(productSku, ValidationGroups.Update.class);

        final var mongoProductSku = getDozerMapperRegistry().map(productSku, MongoProductSku.class);
        final var saved = getDatastore().save(mongoProductSku);

        return getDozerMapperRegistry().map(saved, ProductSku.class);
    }

    @Override
    public void deleteProductSku(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product SKU with id: " + id);
        }

        final DeleteResult deleteResult = getDatastore().find(MongoProductSku.class)
                .filter(eq("_id", new ObjectId(id)))
                .delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Product SKU not found: " + id);
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
