package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import static com.google.common.base.Strings.nullToEmpty;

import jakarta.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoLargeObjectDao implements LargeObjectDao {

    private ValidationHelper validationHelper;
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;
    private MapperRegistry dozerMapperRegistry;

    public MongoLargeObjectDao() {
    }

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        if (!ObjectId.isValid(objectId)) return Optional.empty();

        final var query = getDatastore().find(MongoLargeObject.class);
        query.filter(and(
                eq("_id", new ObjectId(objectId))
        ));

        return Optional.ofNullable(query.first()).map(this::transform);
    }

    @Override
    public Pagination<LargeObject> getLargeObjects(final int offset, final int count, final String search) {

        final Query<MongoLargeObject> query = getDatastore().find(MongoLargeObject.class);

        final String trimmedQueryString = nullToEmpty(search).trim();

        if (!trimmedQueryString.isEmpty()) {
            query.filter(
                    or(
                            Filters.regex("path", Pattern.compile(search)),
                            Filters.regex("mimeType", Pattern.compile(search))
                    )
            );
        }

        return getMongoDBUtils().paginationFromQuery(query,
                offset,
                count,
                u -> getDozerMapper().map(u, LargeObject.class),
                new FindOptions());
    }

    @Override
    public LargeObject createLargeObject(final LargeObject largeObject) {
        getValidationHelper().validateModel(largeObject, ValidationGroups.Insert.class);

        MongoLargeObject mongoLargeObject = transform(largeObject);

        try {
            getDatastore().insert(mongoLargeObject);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        return getLargeObject(mongoLargeObject.getId().toHexString());
    }

    @Override
    public LargeObject updateLargeObject(final LargeObject largeObject) {
        getValidationHelper().validateModel(largeObject, ValidationGroups.Update.class);

        final var query = getDatastore().find(MongoLargeObject.class);
        query.filter(eq("_id", new ObjectId(largeObject.getId())));

        final var options = new ModifyOptions().upsert(false).returnDocument(AFTER);
        final var mongoLargeObject = mongoDBUtils.perform(ds ->
                query.modify(options,
                        set("mimeType", largeObject.getMimeType()),
                        set("url", largeObject.getUrl()),
                        set("path", largeObject.getPath()),
                        set("state", largeObject.getState()),
                        set("lastModified", new Date()),
                        set("accessPermissions", largeObject.getAccessPermissions())
                )
        );

        return transform(mongoLargeObject);
    }

    //TODO: flag it as delete with exp date (in future)
    @Override
    public void deleteLargeObject(final String objectId) {

        final var query = getDatastore().find(MongoLargeObject.class);

        query.filter(and(
            eq("_id", new ObjectId(objectId))
        ));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("LargeObject not found: " + objectId);
        } else if (deleteResult.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }

    public Optional<MongoLargeObject> findMongoLargeObject(String objectId) {
        return Optional.ofNullable(getMongoLargeObject(objectId));
    }

    public MongoLargeObject getMongoLargeObject(String objectId) {
        final var query = getDatastore().find(MongoLargeObject.class);
        query.filter(and(
                eq("_id", new ObjectId(objectId))
        ));

        return query.first();
    }

    private LargeObject transform(final MongoLargeObject mongoLargeObject) {
        return getDozerMapper().map(mongoLargeObject, LargeObject.class);
    }

    private MongoLargeObject transform(final LargeObject largeObject) {
        return getDozerMapper().map(largeObject, MongoLargeObject.class);
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
}
