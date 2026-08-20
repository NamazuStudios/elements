package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductSkuSchema;
import dev.getelements.elements.sdk.Event;
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
import java.util.function.Consumer;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.setOnInsert;

public class MongoProductSkuSchemaDao implements ProductSkuSchemaDao {

    private Datastore datastore;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private Consumer<Event> eventPublisher;

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

        // Snapshot existence before the upsert so we can tell whether this call actually inserted a new
        // document (setOnInsert leaves existing documents untouched, so it is never an "update").
        final var existing = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("schema", schema))
                .first();

        final var query = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("schema", schema));

        final var result = new UpdateBuilder().with(
                setOnInsert(Map.of("schema", schema))
        ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER));

        final var productSkuSchema = getDozerMapperRegistry().map(result, ProductSkuSchema.class);

        if (existing == null) {
            getEventPublisher().accept(Event.builder()
                    .argument(productSkuSchema)
                    .named(PRODUCT_SKU_SCHEMA_CREATED)
                    .build());
        }

        return productSkuSchema;
    }

    @Override
    public void deleteProductSkuSchema(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product SKU Schema with id: " + id);
        }

        final var mongo = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("_id", new ObjectId(id)))
                .first();

        final var deleteResult = getDatastore().find(MongoProductSkuSchema.class)
                .filter(eq("_id", new ObjectId(id)))
                .delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Product SKU Schema not found: " + id);
        }

        getEventPublisher().accept(Event.builder()
                .argument(getDozerMapperRegistry().map(mongo, ProductSkuSchema.class))
                .named(PRODUCT_SKU_SCHEMA_DELETED)
                .build());
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

    public Consumer<Event> getEventPublisher() {
        return eventPublisher;
    }

    @Inject
    public void setEventPublisher(Consumer<Event> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

}
