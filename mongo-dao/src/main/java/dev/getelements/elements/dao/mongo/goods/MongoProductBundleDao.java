package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductBundle;
import dev.getelements.elements.sdk.dao.ProductBundleDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductBundle;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;

import dev.morphia.query.filters.Filter;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.in;
import static dev.morphia.query.filters.Filters.regex;

public class MongoProductBundleDao implements ProductBundleDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<ProductBundle> getProductBundles(final int offset, final int count) {
        final var query = getDatastore().find(MongoProductBundle.class);
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongo -> getDozerMapperRegistry().map(mongo, ProductBundle.class),
                new FindOptions());
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final int offset, final int count) {
        final var app = getMongoApplicationDao().getMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoProductBundle.class);
        query.filter(eq("application._id", app.getObjectId()));
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongo -> getDozerMapperRegistry().map(mongo, ProductBundle.class),
                new FindOptions());
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final String schema,
                                                       final int offset, final int count) {
        final var app = getMongoApplicationDao().getMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoProductBundle.class);
        query.filter(and(
                eq("application._id", app.getObjectId()),
                eq("schema", schema)));
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongo -> getDozerMapperRegistry().map(mongo, ProductBundle.class),
                new FindOptions());
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(
            final String applicationNameOrId, final String schema,
            final String productId, final List<String> tags,
            final int offset, final int count) {
        final var query = getDatastore().find(MongoProductBundle.class);
        final var filters = new ArrayList<Filter>();
        if (applicationNameOrId != null && !applicationNameOrId.isBlank()) {
            try {
                final var app = getMongoApplicationDao().getMongoApplication(applicationNameOrId);
                filters.add(eq("application._id", app.getObjectId()));
            } catch (NotFoundException e) {
                return Pagination.empty();
            }
        }
        if (schema != null && !schema.isBlank()) {
            filters.add(eq("schema", schema));
        }
        if (productId != null && !productId.isBlank()) {
            filters.add(regex("productId", Pattern.compile(Pattern.quote(productId), Pattern.CASE_INSENSITIVE)));
        }
        if (tags != null) {
            final var nonBlankTags = tags.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .toList();
            if (!nonBlankTags.isEmpty()) {
                filters.add(in("tags", nonBlankTags));
            }
        }
        if (!filters.isEmpty()) {
            query.filter(and(filters.toArray(new Filter[0])));
        }
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongo -> getDozerMapperRegistry().map(mongo, ProductBundle.class),
                new FindOptions());
    }

    @Override
    public Pagination<ProductBundle> getProductBundlesByTag(final String tag, final int offset, final int count) {
        final var query = getDatastore().find(MongoProductBundle.class);
        query.filter(eq("tags", tag));
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongo -> getDozerMapperRegistry().map(mongo, ProductBundle.class),
                new FindOptions());
    }

    @Override
    public ProductBundle getProductBundle(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product Bundle with id: " + id);
        }

        final var mongo = getDatastore().find(MongoProductBundle.class)
                .filter(eq("_id", new ObjectId(id)))
                .first();

        if (mongo == null) {
            throw new NotFoundException("Unable to find Product Bundle with id: " + id);
        }

        return getDozerMapperRegistry().map(mongo, ProductBundle.class);
    }

    @Override
    public ProductBundle getProductBundle(final String applicationNameOrId, final String schema, final String productId) {

        final var app = getMongoApplicationDao().getMongoApplication(applicationNameOrId);

        final var mongo = getDatastore().find(MongoProductBundle.class)
                .filter(and(
                        eq("application._id", app.getObjectId()),
                        eq("schema", schema),
                        eq("productId", productId)))
                .first();

        if (mongo == null) {
            throw new NotFoundException(
                    "Unable to find Product Bundle for application=" + applicationNameOrId
                    + " schema=" + schema + " productId=" + productId);
        }

        return getDozerMapperRegistry().map(mongo, ProductBundle.class);
    }

    @Override
    public ProductBundle createProductBundle(final ProductBundle bundle) {

        getValidationHelper().validateModel(bundle, ValidationGroups.Insert.class);

        final var mongo = getDozerMapperRegistry().map(bundle, MongoProductBundle.class);

        try {
            getDatastore().insert(mongo);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) throw new DuplicateException(e);
            throw e;
        }

        return getDozerMapperRegistry().map(mongo, ProductBundle.class);
    }

    @Override
    public ProductBundle updateProductBundle(final ProductBundle bundle) {

        getValidationHelper().validateModel(bundle, ValidationGroups.Update.class);

        if (nullToEmpty(bundle.getId()).isBlank() || !ObjectId.isValid(bundle.getId())) {
            throw new NotFoundException("Product Bundle not found: " + bundle.getId());
        }

        final var existing = getDatastore().find(MongoProductBundle.class)
                .filter(eq("_id", new ObjectId(bundle.getId())))
                .first();

        if (existing == null) {
            throw new NotFoundException("Product Bundle not found: " + bundle.getId());
        }

        final var mongo = getDozerMapperRegistry().map(bundle, MongoProductBundle.class);
        // Preserve the full application sub-document so the unique index is not disturbed
        mongo.setApplication(existing.getApplication());

        final var saved = getDatastore().save(mongo);

        return getDozerMapperRegistry().map(saved, ProductBundle.class);
    }

    @Override
    public void deleteProductBundle(final String id) {

        if (nullToEmpty(id).isBlank() || !ObjectId.isValid(id)) {
            throw new NotFoundException("Unable to find Product Bundle with id: " + id);
        }

        final DeleteResult deleteResult = getDatastore().find(MongoProductBundle.class)
                .filter(eq("_id", new ObjectId(id)))
                .delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Product Bundle not found: " + id);
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

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

}
