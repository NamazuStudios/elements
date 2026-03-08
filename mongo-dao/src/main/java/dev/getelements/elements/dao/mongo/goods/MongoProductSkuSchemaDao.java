package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductSkuSchema;
import dev.getelements.elements.sdk.dao.ProductSkuSchemaDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.setOnInsert;

public class MongoProductSkuSchemaDao implements ProductSkuSchemaDao {

    private Datastore datastore;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Pagination<ProductSkuSchema> getProductSkuSchemas(final int offset, final int count) {

        final var query = getDatastore().find(MongoProductSkuSchema.class);

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoProductSkuSchema -> getDozerMapperRegistry().map(mongoProductSkuSchema, ProductSkuSchema.class),
                new FindOptions());
    }

    @Override
    public ProductSkuSchema getProductSkuSchema(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product SKU Schema with id: " + id);
        }

        final var mongo = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("_id", new ObjectId(id)))
                .first();

        if (mongo == null) {
            throw new NotFoundException("Product SKU Schema not found: " + id);
        }

        return getDozerMapperRegistry().map(mongo, ProductSkuSchema.class);
    }

    @Override
    public ProductSkuSchema createProductSkuSchema(final ProductSkuSchema productSkuSchema) {
        return ensureProductSkuSchema(productSkuSchema.schema());
    }

    @Override
    public ProductSkuSchema ensureProductSkuSchema(final String schema) {

        final var query = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("schema", schema));

        final var result = new UpdateBuilder().with(
                setOnInsert(Map.of("schema", schema))
        ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER));

        return getDozerMapperRegistry().map(result, ProductSkuSchema.class);
    }

    @Override
    public void deleteProductSkuSchema(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product SKU Schema with id: " + id);
        }

        final DeleteResult deleteResult = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("_id", new ObjectId(id)))
                .delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Product SKU Schema not found: " + id);
        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
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
